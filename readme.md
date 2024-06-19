h2 db 연동하는 법
1. h2 db 2.2.224 버전 설치
2. src/main/resources/application.yml 파일에서
      spring:
        datasource:
          url: jdbc:h2:~/ArtFusion
3. 수정 후 프로젝트 실행 = 해당 위치에 ArtFusion 파일 생성
4. mac 기준 - 설치한 h2 파일을 찾아서 h2/bin/h2.sh 실행
5. localhost:8082 포트 창이 뜨면
6. jdbc url에 jdbc:h2:~/ArtFusion 연결 시험 클릭 -> 초록불로 정상 승인되면
7. jdbc:h2:tcp://localhost/~/ArtFusion 로 변경 후 연결 클릭
8. application.yml 파일에서 
     spring:
       datasource: 
         url: jdbc:h2:tcp://localhost/~/ArtFusion
9. 수정 후 프로젝트 실행 = tcp를 통해 접근
10. 완료 / 모르는 부분 김현민에게 문의