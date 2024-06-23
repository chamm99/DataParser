import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // 통합 정보 강의 테이블 insert
        String query = "INSERT INTO current_lecture (lect_name, lect_time, lect_room, cmp_div, credit, is_cyber, " +
                "grade, semester_year, department, professor, code_section, code, notice) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, jsonObject.getString("SBJ_NM"));

            // LECT_TIME_ROOM 값을 파싱
            String lectTimeRoom = jsonObject.optString("LECT_TIME_ROOM", null);
            String modifiedTime = processString(deleteParentheses(MonthStringParsing(lectTimeRoom)));


            // Unique lecture rooms 추출
            String uniqueLectureRooms = extractUniqueLectureRooms(lectTimeRoom);

            // codeSection parsing
            String code = jsonObject.optString("SBJ_DIVCLS", null).split("-")[0];

            preparedStatement.setString(2, modifiedTime); // 파싱된 값 사용 (없으면 null)
            preparedStatement.setString(3, uniqueLectureRooms); // 중복 없는 강의실 정보 사용
            preparedStatement.setString(4, jsonObject.optString("CMP_DIV_NM", null));
            preparedStatement.setInt(5, jsonObject.optInt("CDT", 0));
            preparedStatement.setString(6, jsonObject.optString("CYBER_YN", null));
            preparedStatement.setInt(7, jsonObject.optInt("CRS_SHYR", 0));
            preparedStatement.setString(8, semesterYear);
            preparedStatement.setString(9, jsonObject.optString("EST_DEPT_INFO", null));
            preparedStatement.setString(10, jsonObject.optString("STAFF_NM", null));
            preparedStatement.setString(11, jsonObject.optString("SBJ_DIVCLS", null));
            preparedStatement.setString(12, code);
            preparedStatement.setString(13, jsonObject.optString("TLSN_RMK", null));
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '월7 월8 월9 월10 월11' where lect_name = '서양사의이해'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '금4,금5 금6 금7 금8 금9' where lect_name = '비즈니스프로그래밍'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '수10 수11 수12 수13 수14' where lect_name = '빅데이터캡스톤디자인'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '화7 화8 화9 화10' where lect_name = '빅데이터와딥러닝'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '수6 수7 수8 수9' where lect_name = '선형대수학'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '화6 화7 화8 화9' where lect_name = '지능형영상처리'");
            preparedStatement.executeUpdate("UPDATE current_lecture SET lect_time = '목F' where lect_name = '성공학특강'");
            preparedStatement.executeUpdate();
        }
    }

    private void insertToEverytimeTable(Connection connection, JSONObject jsonObject, String semesterYear) throws SQLException {
        String query = "INSERT INTO previous_lecture (lect_name, cmp_div, is_cyber, credit, subject_type, semester_year, code, notice, professor) " +
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

    public String extractSemesterYear(String fileName) {
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        String semesterYear = baseName.replaceAll("[^0-9_]", "");

        return semesterYear;
    }

    public Optional<String> parseSchedule(String input) {
        if (input == null) {
            return Optional.empty();
        }

        List<String> result = new ArrayList<>();

        String pattern = "(월|화|수|목|금|토|일)(\\d+(?:,\\d+)*)\\s*\\([A-Z]\\d+\\)";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input);

        while (m.find()) {
            String day = m.group(1);
            String times = m.group(2);

            String[] timeArray = times.split(",");
            for (String time : timeArray) {
                result.add(day + time);
            }
        }

        String parsedResult = String.join(" ", result);
        return parsedResult.isEmpty() ? Optional.empty() : Optional.of(parsedResult);
    }

    public String extractUniqueLectureRooms(String lectTimeRoom) {
        if (lectTimeRoom == null || lectTimeRoom.isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(lectTimeRoom);

        Set<String> uniqueRooms = new HashSet<>();

        while (matcher.find()) {
            uniqueRooms.add(matcher.group(1));
        }

        return String.join(" ", uniqueRooms);
    }
    private static String MonthStringParsing(String lectTimeRoom) {
        if (lectTimeRoom == null || lectTimeRoom.isEmpty()) {
            return "";
        }
        // 문자열을 배열에 저장하기 위한 리스트 생성
        List<String> scheduleItems = new ArrayList<>();

        // 여러 패턴을 사용하기 위한 배열
        String[] patterns = {
                "([월화수목금토일]\\d+),(\\d+),(\\d+)\\(([^)]+)\\)",
                "([월화수목금토일]\\d+),(\\d+)\\(([^)]+)\\)",
                "([월화수목금토일]\\d+)\\(([^)]+)\\)",
        };

        // 각 패턴에 대해 매칭된 부분을 리스트에 추가
        for (int i = 0; i < patterns.length; i++) {
            Pattern pattern = Pattern.compile(patterns[i]);
            Matcher matcher = pattern.matcher(lectTimeRoom);

            while (matcher.find()) {
                // 패턴에 매칭된 경우
                if (matcher.group().matches(patterns[i])) {
                    // 요일 추출 (예: "금7,8,9(T301)"에서 "금")
                    String dayOfWeek = matcher.group().substring(0, 1);

                    // 숫자와 괄호 안의 내용 추출
                    Pattern innerPattern = Pattern.compile("\\d+|\\([^)]+\\)");
                    Matcher innerMatcher = innerPattern.matcher(matcher.group());

                    // 숫자와 괄호 안의 내용을 리스트에 추가
                    while (innerMatcher.find()) {
                        // 숫자인 경우 리스트에 추가
                        if (innerMatcher.group().matches("\\d+")) {
                            scheduleItems.add(dayOfWeek + innerMatcher.group());
                        } else {
                            // 괄호 안의 내용인 경우 리스트에 추가
                            scheduleItems.add(innerMatcher.group());
                        }
                    }

                }
            }
        }
        return scheduleItems.toString();
    }
    public static String processString(String input) {
        String removedBrackets = input.replaceAll("[\\[\\]]", "");
        String removedCommas = removedBrackets.replaceAll(",", "");
        String singleSpace = removedCommas.replaceAll("\\s+", " ");
        return singleSpace;
    }
    public static String deleteParentheses(String modifiedTime) {
        StringBuilder result = new StringBuilder();

        // modifiedTime에서 괄호로 둘러싸인 문자열 삭제하여 result에 저장
        return result.append(modifiedTime.replaceAll("\\([^\\)]+\\)", "")).toString();
    }
}
