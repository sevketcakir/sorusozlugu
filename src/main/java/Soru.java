import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.SimpleStringProperty;

@DatabaseTable(tableName = "sorular")
public class Soru {

    @DatabaseField(id=true)
    private String sorumetni;
    @DatabaseField
    private String cevap;

    public Soru() {
    }

    public Soru(String sorumetni, String cevap) {
        this.sorumetni = sorumetni;
        this.cevap = cevap;
    }

    public String getSorumetni() {
        return sorumetni;
    }

    public void setSorumetni(String sorumetni) {
        this.sorumetni = sorumetni;
    }

    public String getCevap() {
        return cevap;
    }

    public void setCevap(String cevap) {
        this.cevap = cevap;
    }
}
