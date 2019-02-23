import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.List;

public class SoruApp {
    public static void main(String[] args) throws Exception {
        String databaseUrl="jdbc:sqlite:sorular.sqlite";
        ConnectionSource connectionSource=new JdbcConnectionSource(databaseUrl);
        Dao<Soru, String> soruDao =
                DaoManager.createDao(connectionSource, Soru.class);
        TableUtils.createTableIfNotExists(connectionSource, Soru.class);

        QueryBuilder<Soru, String> queryBuilder=soruDao.queryBuilder();
        queryBuilder.where().like("sorumetni", "%a%");
        PreparedQuery<Soru> preparedQuery=queryBuilder.prepare();
        List<Soru> sorular=soruDao.query(preparedQuery);
        for(Soru s : sorular) {
            System.out.println(s.getSorumetni());
            System.out.println(s.getCevap());
        }

    }
}
