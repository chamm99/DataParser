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
            //파일명에서 확장자 제거하여 학년 학기 추출
            String semesterYear = extractSemesterYear(fileName);

            // Txt -> String
            String jsonString = readJsonTxt(fileName);

            // Xml -> JsonObjects
            List<JSONObject> jsonObjects = xmlToJsonObjects("subject", jsonString);

            //JsonObjects -> EverytimeTable
            for (JSONObject jsonObject : jsonObjects) {
                insertToEverytimeTable(conn, jsonObject, semesterYear);
            }

            System.out.println("<" + fileName + "> inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertCurrentLecturesTable(Connection conn, String fileName, String semesterYear) {
        //통합 정보 포털 강의 정보 insert
        try {
            String jsonString = readJsonTxt(fileName);

            //파일 읽어 json 형식으로 변환한뒤 DB에 삽입
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                insertToCurrentLecturesTable(conn, jsonArray.getJSONObject(i), semesterYear);
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

    public Connection createConn(String url, String user, String password) {
        //커넥션 생성
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("====== Connecting To Database ======");
            conn = DriverManager.getConnection(url,
                    user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // 현학기 강의 목록 삽입
    private void insertToCurrentLecturesTable(Connection connection, JSONObject jsonObject, String semesterYear) throws SQLException {
        //통합 정보 강의 테이블 insert
        String query = "INSERT INTO current_lectures (lect_name, lect_time, lect_room, cmp_div, credit, is_cyber, " +
                "grade, semester_year, code, notice) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, jsonObject.getString("SBJ_NM"));
            preparedStatement.setString(2, jsonObject.optString("LECT_TIME_ROOM", null));
            preparedStatement.setString(3, jsonObject.optString("LECT_TIME_ROOM", null));
            preparedStatement.setString(4, jsonObject.optString("CMP_DIV_NM", null));
            preparedStatement.setInt(5, jsonObject.optInt("CDT", 0));
            preparedStatement.setString(6, jsonObject.optString("CYBER_YN", null));
            preparedStatement.setString(7, jsonObject.optString("EST_DEPT_INFO", null));
            preparedStatement.setString(8, semesterYear);
            preparedStatement.setString(9, jsonObject.optString("SBJ_DIVCLS", null));
            preparedStatement.setString(10, jsonObject.optString("TLSN_RMK", null));
            preparedStatement.executeUpdate();
        }
    }
    private void insertToEverytimeTable(Connection connection, JSONObject jsonObject, String semesterYear) throws SQLException {
        String query = "INSERT INTO previous_lectures (lect_rename, cmp_div, is_cyber, credit, subject_type, semester_year, lect_id, notice, professor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, jsonObject.optString("name", null));
            preparedStatement.setString(2, jsonObject.optString("type", null));
            preparedStatement.setInt(3, jsonObject.optInt("is_cyber", 0));
            preparedStatement.setInt(4, jsonObject.optInt("credit", 0));
            preparedStatement.setString(5, jsonObject.optString("subject_type", null));
            preparedStatement.setString(6, semesterYear);
            preparedStatement.setString(7, jsonObject.optString("code", null));
            preparedStatement.setString(8, jsonObject.optString("notice", null));
            preparedStatement.setString(9, jsonObject.optString("professor", null));
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

    public String extractSemesterYear(String fileName) {
        // 파일명에서 확장자 제거
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        // baseName에서 숫자와 언더바 추출
        String semesterYear = baseName.replaceAll("[^0-9_]", "");

        return semesterYear;
    }

}
