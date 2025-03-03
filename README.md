## 모두를 위한 채팅 애플리케이션
#### 개발기간 : 25.01.07 ~ 25.02.06

### 📄 프로젝트 개요

프로젝트명: 모두를 위한 채팅 애플리케이션

목적: 간단하고 직관적인 채팅 앱 구현, 실시간 메시지 송수신 및 알림 기능 제공

목표 사용자: 일반 사용자, 커뮤니티, 그룹 활동 사용자

페르소나: 직장인 김대리 - 업무 중 팀원들과 빠르게 의견을 교환하고 파일을 공유할 수 있는 채팅 툴 필요

학생 이서준 - 동아리 및 그룹 활동에서 빠르게 친구들과 소통하고, 파일이나 일정 공유 기능이 필요한 사용자

## 🛠️ 구현기능
### 1. 파일 관리 (File Management) - 프로필 이미지, 채팅 이미지 전송
 - AWS S3를 활용한 이미지 업로드, 다운로드, 삭제 기능 구현
   
 - AWS Lambda를 이용한 리사이징된 이미지 썸네일 자동 생성(webp로 생성)

- 보안 강화를 위해 다음과 같은 파일 검증 로직 적용:
    * 파일 확장자 검증 및 HTML 태그 제거
    * Apache Tika를 이용한 MIME 타입 검사(확장자 변조 공격 방지)
 
### 2. 알림 시스템 (Notification System)
* 채팅방 생성 시 참가자들에게 자동으로 알림을 저장하는 기능 개발

### 3. 검색 기능 (Search System)
* QueryDSL을 활용한 동적 쿼리 기반의 유저 검색 기능 구현
* 페이징 처리 및 데이터베이스 인덱싱 최적화를 적용하여 검색 성능 향상

## 📄 API 명세서

[전체 API 명세서](https://docs.google.com/spreadsheets/d/1G8_AqPqJMMwJgapfmnYmdf429MRi7Tmjv939TC2c8MU/edit?gid=0#gid=0)

<img width="100%" alt="스크린샷 2025-02-16 오전 12 35 52" src="https://github.com/user-attachments/assets/c37850c0-ae46-41bd-bbab-734729bec9fe" />
<img width="100%" alt="스크린샷 2025-02-16 오전 12 36 32" src="https://github.com/user-attachments/assets/ab39489a-8aba-4e5d-86ab-921ebf3663c8" />
<img width="100%" alt="스크린샷 2025-02-16 오전 12 36 21" src="https://github.com/user-attachments/assets/7790eead-8354-4019-a080-ee38ee61c309" />

## ⚙️ 기술스택
### backend
<img src="https://img.shields.io/badge/java-20232a.svg?style=for-the-badge&logo=java&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/jpa-20232a.svg?style=for-the-badge&logo=jpa&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/springboot-20232a.svg?style=for-the-badge&logo=spring Boot&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/springsecurity-20232a.svg?style=for-the-badge&logo=springsecurity&logoColor=#6DB33F" /> <img src="https://img.shields.io/badge/websocket-20232a.svg?style=for-the-badge&logo=websocket&logoColor=#6DB33F" />

### database
<img src="https://img.shields.io/badge/mysql-20232a.svg?style=for-the-badge&logo=mysql&logoColor=##4479A1" /> <img src="https://img.shields.io/badge/mongodb-20232a.svg?style=for-the-badge&logo=mongodb&logoColor=#47A248" /> <img src="https://img.shields.io/badge/redis-20232a.svg?style=for-the-badge&logo=redis&logoColor=##FF4438" /> 

### cloud
<img src="https://img.shields.io/badge/amazons3-20232a.svg?style=for-the-badge&logo=amazons3&logoColor=#569A31" /> <img src="https://img.shields.io/badge/awslambda-20232a.svg?style=for-the-badge&logo=awslambda&logoColor=#FF9900" /> 

### deploy
<img src="https://img.shields.io/badge/nginx-20232a.svg?style=for-the-badge&logo=nginx&logoColor=##009639" /> <img src="https://img.shields.io/badge/docker-20232a.svg?style=for-the-badge&logo=docker&logoColor=#2496ED" />


## ⚙️ 아키텍쳐
<img width="929" alt="스크린샷 2025-02-15 오후 3 19 13" src="https://github.com/user-attachments/assets/3041644e-9e42-4bfa-a4f0-ad2ee9f29543" />

## ⚙️ ERD
<img width="1135" alt="스크린샷 2025-02-14 오후 11 43 01" src="https://github.com/user-attachments/assets/4179967b-656f-4faa-8c49-742a47eba49e" />

## 🚨 트러블슈팅
[Nginx 크기 제한 설정](https://velog.io/@eunoia73/spring-Nginx-%ED%81%AC%EA%B8%B0-%EC%A0%9C%ED%95%9C-%EC%84%A4%EC%A0%95)

[한글 파일명 길이 제한으로 인한 파일 업로드 실패](https://velog.io/@eunoia73/Spring-org.springframework.dao.DataIntegrityViolationException)

[JPA 트랜잭션 미설정으로 인한 알림 삭제 작업 실패](https://velog.io/@eunoia73/JPA-org.springframework.dao.InvalidDataAccessApiUsageException)

[테스트 데이터 삽입 중 발생한 EntityExistsException 문제](https://velog.io/@eunoia73/JPA-jakarta.persistence.EntityExistsException-detached-entity-passed-to-persist)

## 📝 메모
[S3 버킷 생성 및 권한 설정](https://velog.io/@eunoia73/AWS-S3-%EB%B2%84%ED%82%B7-%EC%83%9D%EC%84%B1-%EB%B0%8F-%EA%B6%8C%ED%95%9C-%EC%84%A4%EC%A0%95)

[IAM 액세스 키 발급받기](https://velog.io/@eunoia73/AWS-IAM-%EC%95%A1%EC%84%B8%EC%8A%A4-%ED%82%A4-%EB%B0%9C%EA%B8%89%EB%B0%9B%EA%B8%B0)

[Lambda timeout 오류](https://velog.io/@eunoia73/AWS-Lambda-timeout-%EC%98%A4%EB%A5%98)


## 🎬 시연 영상 

### 1. 프로필 이미지 등록

https://github.com/user-attachments/assets/b2257c43-5e0b-428c-bb94-9e19e29cb763


### 2-1. 채팅 이미지 발신

https://github.com/user-attachments/assets/11a86e05-2cca-4fd7-83ce-efa83e1926b9

### 2-2. 채팅 이미지 수신

https://github.com/user-attachments/assets/daac66aa-6c12-434f-b7c7-f16585e0a8d6


### 3. 유저 검색

https://github.com/user-attachments/assets/e0e8a2c7-508b-471f-8a31-02183589fc1f


## 🔗 배포 링크
https://kdt-pt-1-pj-1-team01.elicecoding.com/login

#### test 계정

아이디 : elice@example.com <br>
비밀번호 : example

## ✅ 확장 방안
S3 파일 캐싱: 자주 사용되는 파일에 대해 캐싱을 적용하여 파일 다운로드 성능 개선

실시간 알림 기능 구현: SSE를 사용하여 실시간 알림을 제공할 수 있는 시스템을 추가. 이를 통해 사용자가 즉시 알림을 받을 수 있도록 개선

검색 기능 캐싱: (Redis 등)을 추가하여 검색 속도와 확장성을 개선
