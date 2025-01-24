-- 일반 회원가입 사용자 샘플 데이터
INSERT INTO user_entity (nickname, email, password, is_first_login, role, profile_img, activated, oauth_provider, oauth_id, oauth_token) 
VALUES 
('홍길동', 'user1@test.com', '{bcrypt}$2a$10$7WKp8dE3BI7OQHqLLIIP9.FIhUaKw2a/2TCWIQ/kmu85Kw6VyjWIW', true, 'ROLE_USER',  null, true, null, null, null),
('김철수', 'user2@test.com', '{bcrypt}$2a$10$7WKp8dE3BI7OQHqLLIIP9.FIhUaKw2a/2TCWIQ/kmu85Kw6VyjWIW', false, 'ROLE_USER', null, true, null, null, null),
('김영희', 'user3@test.com', '{bcrypt}$2a$10$7WKp8dE3BI7OQHqLLIIP9.FIhUaKw2a/2TCWIQ/kmu85Kw6VyjWIW', false, 'ROLE_ADMIN', null, true, null, null, null);

-- OAuth 로그인 사용자 샘플 데이터
INSERT INTO user_entity (nickname, email, password, is_first_login, role, profile_img, activated, oauth_provider, oauth_id, oauth_token) 
VALUES 
('구글사용자', 'google@gmail.com', null, false, 'ROLE_USER', 'google.jpg', true, 'google', 'google123', null),
('카카오사용자', 'kakao@kakao.com', null, false, 'ROLE_USER', 'kakao.jpg', true, 'kakao', 'kakao123', null);

-- 채팅방 --
INSERT INTO room (id, room_id, room_name, room_type, created_at)
VALUES
(1, 'a9530082-0587-44f8-b1c2-d5189e4cec7d', '그룹 채팅방', 'GM', NOW()),
(2, '78a56e67-322d-40b6-bc45-b1002ffd93ff','개인 채팅방', 'DM', NOW());

-- 친구 관계 샘플 데이터
INSERT INTO friendship (user_id, friend_id, status, created_at)
VALUES 
(1, 2, 'ACCEPTED', NOW()),
(2, 1, 'ACCEPTED', NOW()),
(1, 4, 'PENDING', NOW()); 