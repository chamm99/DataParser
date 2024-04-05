import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Service {
    private Connection conn = null;

    public void insertEverytimeTable(Connection conn, String fileName) {

        //Everytime 이전 강의 정보 삽입
        try {
            //파일명에서 확장자 제거하여 테이블명 생성
            String tableName = fileName.substring(0, fileName.length()-4);

            //에브리타임 테이블 생성
            createEverytimeLecturesTable(tableName, conn);

            // Txt -> String
            String jsonString = readJsonTxt(fileName);

            // Xml -> JsonObjects
            List<JSONObject> jsonObjects = xmlToJsonObjects("subject", jsonString);

            //JsonObjects -> EverytimeTable
            for (JSONObject jsonObject : jsonObjects) {
                insertToEverytimeTable(conn, jsonObject, tableName);
            }

            System.out.println("<" + fileName + "> inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertCurrentLecturesTable(Connection conn, String fileName, String tableName) {
        //통합 정보 포털 강의 정보 insert
        try {
            //강의 테이블 생성
            createCurrentLecturesTable(tableName, conn);
            String jsonString = readJsonTxt(fileName);

            //파일 읽어 json 형식으로 변환한뒤 DB에 삽입
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                insertToCurrentLecturesTable(conn, jsonArray.getJSONObject(i), tableName);
            }
            System.out.println("<" + fileName + "> inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String readJsonTxt(String fileName) {
        // json 형식 Txt -> String 변환
        StringBuilder jsonContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonContent.toString();
    }

    private List<JSONObject> xmlToJsonObjects(String key, String xml) {
        //XML -> json 변환
        JSONArray jsonArray = XML.toJSONObject(xml).getJSONArray(key);
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjects.add(jsonArray.getJSONObject(i));
        }

        return jsonObjects;
    }

    public Connection createConn(String database, String user, String password) {
        //커넥션 생성
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("====== Connecting To Database ======");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + database + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
                    user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    private void insertToCurrentLecturesTable(Connection connection, JSONObject jsonObject, String tableName) throws SQLException {
        //통합 정보 강의 테이블 insert
        String query = "INSERT INTO "+ tableName +" (SBJ_NO, SBJ_NM, LECT_TIME_ROOM, CMP_DIV_RCD, THEO_TIME, ATTC_FILE_NO, DIVCLS, TLSN_RMK, CDT, SES_RCD, CMP_DIV_NM, CYBER_YN, CYBER_B_YN, SCH_YEAR, PRAC_TIME, CYBER_S_YN, FILE_PBY_YN, KIND_RCD, SBJ_DIVCLS, STAFF_NM, DEPT_CD, RMK, CYBER_E_YN, REP_STAFF_NO, EST_DEPT_INFO, SMT_RCD, CRS_SHYR, KIND_NM, BEF_CTNT_02, BEF_CTNT_01)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, jsonObject.getString("SBJ_NO"));
            preparedStatement.setString(2, jsonObject.getString("SBJ_NM"));
            preparedStatement.setString(3, jsonObject.optString("LECT_TIME_ROOM", null));
            preparedStatement.setString(4, jsonObject.optString("CMP_DIV_RCD", null));
            preparedStatement.setInt(5, jsonObject.optInt("THEO_TIME", 0));
            preparedStatement.setObject(6, jsonObject.isNull("ATTC_FILE_NO") ? null : jsonObject.getInt("ATTC_FILE_NO"));
            preparedStatement.setInt(7, jsonObject.optInt("DIVCLS", 0));
            preparedStatement.setObject(8, jsonObject.isNull("TLSN_RMK") ? null : jsonObject.getString("TLSN_RMK"));
            preparedStatement.setInt(9, jsonObject.optInt("CDT", 0));
            preparedStatement.setString(10, jsonObject.optString("SES_RCD", null));
            preparedStatement.setString(11, jsonObject.optString("CMP_DIV_NM", null));
            preparedStatement.setString(12, jsonObject.optString("CYBER_YN", null));
            preparedStatement.setString(13, jsonObject.optString("CYBER_B_YN", null));
            preparedStatement.setString(14, jsonObject.optString("SCH_YEAR", null));
            preparedStatement.setInt(15, jsonObject.optInt("PRAC_TIME", 0));
            preparedStatement.setString(16, jsonObject.optString("CYBER_S_YN", null));
            preparedStatement.setString(17, jsonObject.optString("FILE_PBY_YN", null));
            preparedStatement.setString(18, jsonObject.optString("KIND_RCD", null));
            preparedStatement.setString(19, jsonObject.optString("SBJ_DIVCLS", null));
            preparedStatement.setString(20, jsonObject.optString("STAFF_NM", null));
            preparedStatement.setString(21, jsonObject.optString("DEPT_CD", null));
            preparedStatement.setObject(22, jsonObject.isNull("RMK") ? null : jsonObject.getString("RMK"));
            preparedStatement.setString(23, jsonObject.optString("CYBER_E_YN", null));
            preparedStatement.setString(24, jsonObject.optString("REP_STAFF_NO", null));
            preparedStatement.setString(25, jsonObject.optString("EST_DEPT_INFO", null));
            preparedStatement.setString(26, jsonObject.optString("SMT_RCD", null));
            preparedStatement.setInt(27, jsonObject.optInt("CRS_SHYR", 0));
            preparedStatement.setString(28, jsonObject.optString("KIND_NM", null));
            preparedStatement.setObject(29, jsonObject.isNull("BEF_CTNT_02") ? null : jsonObject.getString("BEF_CTNT_02"));
            preparedStatement.setObject(30, jsonObject.isNull("BEF_CTNT_01") ? null : jsonObject.getString("BEF_CTNT_01"));

            preparedStatement.executeUpdate();
        }
    }
    private void insertToEverytimeTable(Connection connection, JSONObject jsonObject, String tableName) throws SQLException {
        //Everytime 강의 정보 insert
        String query = "INSERT INTO "+ tableName + " (code, name, timeplace, type, time, place, credit, popular, notice, lectureRate, misc1, misc2, misc3, misc4)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);\n";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, jsonObject.getString("code"));
            preparedStatement.setString(2, jsonObject.getString("name"));

            // "timeplace" 필드가 배열인 경우
            JSONArray timeplaceArray = jsonObject.optJSONArray("timeplace");
            String timeplaceString = (timeplaceArray != null && timeplaceArray.length() > 0) ? timeplaceArray.getJSONObject(0).toString() : null;
            preparedStatement.setObject(3, timeplaceString);

            preparedStatement.setString(4, jsonObject.getString("type"));
            preparedStatement.setString(5, jsonObject.getString("time"));
            preparedStatement.setString(6, jsonObject.getString("place"));
            preparedStatement.setInt(7, jsonObject.getInt("credit"));
            preparedStatement.setInt(8, jsonObject.getInt("popular"));
            preparedStatement.setString(9, jsonObject.optString("notice", null));
            preparedStatement.setInt(10, jsonObject.getInt("lectureRate"));
            preparedStatement.setString(11, jsonObject.optString("misc1", null));
            preparedStatement.setString(12, jsonObject.optString("misc2", null));
            preparedStatement.setString(13, jsonObject.optString("misc3", null));
            preparedStatement.setString(14, jsonObject.optString("misc4", null));

            preparedStatement.executeUpdate();
        }
    }

    private void createCurrentLecturesTable(String tableName, Connection conn) {
        //통합 정보 포털 강의 데이터 테이블 create
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    "SBJ_NO VARCHAR(50), " +
                    "SBJ_NM VARCHAR(255), " +
                    "LECT_TIME_ROOM VARCHAR(255), " +
                    "CMP_DIV_RCD VARCHAR(50), " +
                    "THEO_TIME INT, " +
                    "ATTC_FILE_NO VARCHAR(50), " +
                    "DIVCLS INT, " +
                    "TLSN_RMK VARCHAR(255), " +
                    "CDT INT, " +
                    "SES_RCD VARCHAR(50), " +
                    "CMP_DIV_NM VARCHAR(255), " +
                    "CYBER_YN VARCHAR(1), " +
                    "CYBER_B_YN VARCHAR(1), " +
                    "SCH_YEAR VARCHAR(4), " +
                    "PRAC_TIME INT, " +
                    "CYBER_S_YN VARCHAR(1), " +
                    "FILE_PBY_YN VARCHAR(1), " +
                    "KIND_RCD VARCHAR(50), " +
                    "SBJ_DIVCLS VARCHAR(50) PRIMARY KEY, " +
                    "STAFF_NM VARCHAR(255), " +
                    "DEPT_CD VARCHAR(50), " +
                    "RMK VARCHAR(255), " +
                    "CYBER_E_YN VARCHAR(1), " +
                    "REP_STAFF_NO VARCHAR(50), " +
                    "EST_DEPT_INFO VARCHAR(255), " +
                    "SMT_RCD VARCHAR(50), " +
                    "CRS_SHYR INT, " +
                    "KIND_NM VARCHAR(255), " +
                    "BEF_CTNT_02 VARCHAR(255), " +
                    "BEF_CTNT_01 VARCHAR(255) " +
                    ")";

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.executeUpdate();
                System.out.println("Table<" + tableName + "> created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createEverytimeLecturesTable(String tableName, Connection conn) {
        // 에브리타임 이전 강의 데이터 테이블 create
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    "code VARCHAR(20) PRIMARY KEY, " +
                    "type VARCHAR(20), " +
                    "target VARCHAR(100), " +
                    "lectureId INT, " +
                    "professor VARCHAR(100), " +
                    "grade VARCHAR(20), " +
                    "name VARCHAR(100), " +
                    "time VARCHAR(100), " +
                    "place VARCHAR(100), " +
                    "credit INT, " +
                    "popular INT, " +
                    "notice TEXT, " +
                    "timeplace JSON, " +
                    "lectureRate INT, " +
                    "misc1 VARCHAR(100), " +
                    "misc2 VARCHAR(100), " +
                    "misc3 VARCHAR(100), " +
                    "misc4 VARCHAR(100) " +
                    ")";

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.executeUpdate();
                System.out.println("Table<" + tableName + "> created successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void dropTable(Connection conn, String tableName) {
            //table 존재할 시 삭제
            String query = "DROP TABLE IF EXISTS " + tableName;

            try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.executeUpdate();
                System.out.println(tableName + " dropped successfully.");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

    }

}
