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




-------더미데이터 세트--------
--user
INSERT INTO user_entity (activated, is_first_login, id, email, nickname, oauth_id, oauth_provider, oauth_token, password, profile_img, role)
VALUES
(true, true, 6, 'tester1000@example.com', 'tester1000', null, null, null, '{bcrypt}$2a$10$deGY2AmmOpACiqUcy/Byae4332jyaAd7L6nN97ao/a7tLdgf24Tla', 'https://sookyung-s3-bucket-resized.s3.ap-northeast-2.amazonaws.com/resized-babea411-1c72-4fee-a407-0ae479c650b5_christmasTree.jpg', 'ROLE_USER'),
(true, true, 7, 'tester1001@example.com', 'tester1001', null, null, null, '{bcrypt}$2a$10$3hFWGFp7I8.3KoVyZcQixOsmFIdefxBF83bXCzGvKS5DqvBoBnXNS', null, 'ROLE_USER'),
(true, true, 8, 'tester1002@example.com', 'tester1002', null, null, null, '{bcrypt}$2a$10$Lmk7T.pvypPth.Z6eU63Y.U7pwwqGmW.QpnVVxY4veJGJrtk2HKrO', null, 'ROLE_USER');

--file
INSERT INTO files (created_at, expired_at, file_size, id, file_id, file_name, file_type, nickname, origin_file_url, room_id, thumb_nail_url, category)
VALUES
('2025-01-26 21:16:28.502257', '2025-02-26 09:00:00.000000', 113460, 1, 'babea411-1c72-4fee-a407-0ae479c650b5', 'babea411-1c72-4fee-a407-0ae479c650b5_christmasTree.jpg', 'image/jpeg', 'tester1000', 'https://sookyung-s3-bucket.s3.ap-northeast-2.amazonaws.com/babea411-1c72-4fee-a407-0ae479c650b5_christmasTree.jpg', null, 'https://sookyung-s3-bucket-resized.s3.ap-northeast-2.amazonaws.com/resized-babea411-1c72-4fee-a407-0ae479c650b5_christmasTree.jpg', 'PROFILE'),
('2025-01-26 21:17:34.200576', '2025-02-26 09:00:00.000000', 113460, 2, 'd136fc85-a3e3-482e-9ebf-bbd1de342397', 'd136fc85-a3e3-482e-9ebf-bbd1de342397_christmasTree.jpg', 'image/jpeg', 'tester1000', 'https://sookyung-s3-bucket.s3.ap-northeast-2.amazonaws.com/d136fc85-a3e3-482e-9ebf-bbd1de342397_christmasTree.jpg', '0519e87f-7ed2-48eb-8195-9f1e94b6fdbd', 'https://sookyung-s3-bucket-resized.s3.ap-northeast-2.amazonaws.com/resized-d136fc85-a3e3-482e-9ebf-bbd1de342397_christmasTree.jpg', 'CHAT');


--chatRoom
INSERT INTO chat_db.room (created_at, id, room_id, room_name, room_type)
VALUES
('2025-01-26 21:16:05.926019', 3, '0519e87f-7ed2-48eb-8195-9f1e94b6fdbd', 'room1000', 'GM');


--participants
INSERT INTO chat_db.participants (chat_room_id, id, user_id, role)
VALUES
(3, 1, 6, 'OWNER'),
(3, 2, 7, 'MEMBER'),
(3, 3, 8, 'MEMBER');


--notification
INSERT INTO chat_db.notification (is_read, created_at, id, message, receiver, room_id, room_name, sender, room_type)
VALUES
(false, '2025-01-26 21:16:05.991201', 1, '새로운 채팅방에 초대되었습니다 : \'room1000\'', '01933f4a-dd7f-4fb1-9f9e-1eab52b471b3', 'tester1001', '0519e87f-7ed2-48eb-8195-9f1e94b6fdbd', 'room1000', 'tester1000', 'GM'),
(false, '2025-01-26 21:16:05.996533', 2, '새로운 채팅방에 초대되었습니다 : \'room1000\'', 'dff50518-f482-40c5-b459-f2bcb10166fe', 'tester1002', '0519e87f-7ed2-48eb-8195-9f1e94b6fdbd', 'room1000', 'tester1000', 'GM');

------------------------