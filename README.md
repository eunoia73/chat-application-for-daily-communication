## 모두를 위한 채팅 애플리케이션
#### 25.01.07 ~ 25.02.06

### 📄 프로젝트 개요

프로젝트명: 모두를 위한 채팅 애플리케이션

목적: 간단하고 직관적인 채팅 앱 구현, 실시간 메시지 송수신 및 알림 기능 제공

목표 사용자: 일반 사용자, 커뮤니티, 그룹 활동 사용자

페르소나: 직장인 김대리 - 업무 중 팀원들과 빠르게 의견을 교환하고 파일을 공유할 수 있는 채팅 툴 필요

학생 이서준 - 동아리 및 그룹 활동에서 빠르게 친구들과 소통하고, 파일이나 일정 공유 기능이 필요한 사용자

### 


## ⚙️ 기술스택
### backend
<img src="https://img.shields.io/badge/java-20232a.svg?style=for-the-badge&logo=java&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/jpa-20232a.svg?style=for-the-badge&logo=jpa&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/springboot-20232a.svg?style=for-the-badge&logo=spring Boot&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/springsecurity-20232a.svg?style=for-the-badge&logo=springsecurity&logoColor=#6DB33F" />

### database
<img src="https://img.shields.io/badge/mysql-20232a.svg?style=for-the-badge&logo=mysql&logoColor=##4479A1" /> <img src="https://img.shields.io/badge/mongodb-20232a.svg?style=for-the-badge&logo=mongodb&logoColor=#47A248" /> 

### cloud
<img src="https://img.shields.io/badge/amazons3-20232a.svg?style=for-the-badge&logo=amazons3&logoColor=#569A31" /> <img src="https://img.shields.io/badge/awslambda-20232a.svg?style=for-the-badge&logo=awslambda&logoColor=#FF9900" /> 




## 🛠️ 구현기능
### 1. 파일 관리 (File Management) - 프로필 이미지, 채팅 이미지 전송
 - AWS S3를 활용한 이미지 업로드, 다운로드, 삭제 기능 구현
   
 - AWS Lambda를 이용한 리사이징된 이미지 썸네일 자동 생성(webp로 생성)

- 보안 강화를 위해 다음과 같은 파일 검증 로직 적용:
    * 파일 확장자 검증 및 HTML 태그 제거
    * Apache Tika를 이용한 MIME 타입 검사(확장자 변조 공격 방지)
 
### 2. 알림 시스템 (Notification System)
* 채팅방 생성 시 참가자들에게 자동으로 알림을 저장하는 기능 개발
* 사용자 경험 향상을 위해 비동기 이벤트 처리 방식 적용

### 3. 검색 기능 (Search System)
* QueryDSL을 활용한 동적 쿼리 기반의 유저 검색 기능 구현
* 페이징 처리 및 데이터베이스 인덱싱 최적화를 적용하여 검색 성능 향상


## ⚙️ ERD
<img width="1135" alt="스크린샷 2025-02-14 오후 11 43 01" src="https://github.com/user-attachments/assets/4179967b-656f-4faa-8c49-742a47eba49e" />

