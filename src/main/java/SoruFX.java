import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class SoruFX extends Application implements Initializable{
    @FXML TextField tfAranacak;
    @FXML TextField tfKarakter;
    @FXML TableView<Soru> tvGoster;
    @FXML TableColumn<Soru, String> tcSoru;
    @FXML TableColumn<Soru, String> tcCevap;
    @FXML Label lbSoruSayisi;
    Stage primaryStage;
    Dao<Soru, String> soruDao;
    FileChooser fileChooser=new FileChooser();
    File selectedFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(SoruFX.class.getResource("SoruFX.fxml"));

        Scene scene = new Scene(root, 600, 600);

        this.primaryStage = primaryStage;
        primaryStage.setTitle("Soru Bankası");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void updateList(String aramaMetni) {
        try {
            QueryBuilder<Soru, String> queryBuilder = soruDao.queryBuilder();
            SelectArg selectArg = new SelectArg();
            Where where = queryBuilder.where().like("sorumetni", selectArg);

            PreparedQuery<Soru> preparedQuery = queryBuilder.orderBy("sorumetni", true).prepare();
            selectArg.setValue("%" + aramaMetni + "%");
            List<Soru> sorular = soruDao.query(preparedQuery);
            tvGoster.setItems(FXCollections.observableArrayList(sorular));
            lbSoruSayisi.setText("Soru sayısı: " + soruDao.countOf());
        }
        catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "ERROR");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel dosyaları (*.xls, *.xlsx)", "*.xls", "*.XLS","*.xlsx", "*.XLSX")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir") + File.separator));

        tvGoster.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tcSoru.setCellValueFactory(new PropertyValueFactory<Soru, String>("sorumetni"));
        tcCevap.setCellValueFactory(new PropertyValueFactory<Soru, String>("cevap"));
        String databaseUrl="jdbc:sqlite:sorular.sqlite";
        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
            soruDao = DaoManager.createDao(connectionSource, Soru.class);
            TableUtils.createTableIfNotExists(connectionSource, Soru.class);

            updateList("%%");
        }
        catch (SQLException exc){exc.printStackTrace();}

        tfAranacak.textProperty().addListener((observable, oldValue, newValue) -> {
            try{
                int l=Integer.parseInt(tfKarakter.getText());
                if(newValue.length()>=l || newValue.length()==0){
                    updateList(newValue);
                }
            }
            catch (Exception exc){
                exc.printStackTrace();
            }
        });
    }

    public void exceldenYukle(ActionEvent event) throws IOException, InvalidFormatException {
        if (selectedFile!=null && selectedFile.getParentFile()!=null)
            fileChooser.setInitialDirectory(selectedFile.getParentFile());
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile==null)
            return;
        long baslangic = System.nanoTime();
        Workbook workbook= WorkbookFactory.create(selectedFile);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        for (Row row: sheet) {
            String soru=dataFormatter.formatCellValue(row.getCell(0));
            String cevap=dataFormatter.formatCellValue(row.getCell(1));
            Soru s=new Soru(soru, cevap);
            try {
                soruDao.create(s);
            } catch (SQLException e) {
                System.out.println("Soru eklenemedi:("+soru+")\nhata:"+e.getMessage());
            }

        }
        workbook.close();

        updateList("%%");

        long gecenSure = TimeUnit.SECONDS.convert(System.nanoTime()-baslangic, TimeUnit.NANOSECONDS);
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Geçen süre");
        alert.setContentText("Geçen süre: "+gecenSure+" sn");
        alert.showAndWait();
    }

    public void temizle(ActionEvent actionEvent) {
        tfAranacak.setText("");
        updateList("%%");
    }

    public void soruSil(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Onay Penceresi");
        alert.setHeaderText("Silme Onayı");
        alert.setContentText("Seçili soruları silmek istiyor musunuz?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            for (Soru s : tvGoster.getSelectionModel().getSelectedItems())
                try {
                    soruDao.delete(s);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                updateList("%%");
        }
    }
}
