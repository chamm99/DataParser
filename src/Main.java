import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Service service = new Service();
        //jdbc 커넥션 생성
        Connection conn = service.createConn("", "", "");

        service.dropTable(conn, "");
        service.dropTable(conn, "");

        //에브리타임 이전 강의 테이블 생성 및 데이터 삽입
        //테이블 명은 파일명에서 ".txt"를 잘라낸 이름으로 자동 생성
        service.insertEverytimeTable(conn, "");

        //현재 강의 테이블 생성 및 데이터 삽입
        service.insertCurrentLecturesTable(conn, "", "");

    }
}