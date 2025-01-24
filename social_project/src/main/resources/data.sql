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
(true, true, 6, 'tester1001@example.com', 'tester1001', null, null, null, '{bcrypt}$2a$10$CELmEbgbHSNjtSiFIIZQUeFwZYZNxHSiV7Uy1gtngNKxqJdOs/rdG', null, 'ROLE_USER'),
(true, true, 7, 'tester1000@example.com', 'tester1000', null, null, null, '{bcrypt}$2a$10$PnrxpLSKx/P88Isin1uuHe6SWPlQiE8SvUfhUEcIwqo3Tg8.bUFEW', null, 'ROLE_USER'),
(true, true, 8, 'tester1002@example.com', 'tester1002', null, null, null, '{bcrypt}$2a$10$v9Mo2Ixm5HGca6o0lugxeOSojlwv4Lw2CsiF148WBG6oI13U5HpDi', null, 'ROLE_USER');

--file
INSERT INTO files (created_at, expired_at, file_size, id, file_id, file_name, file_type, nickname, origin_file_url, room_id, thumb_nail_url, category)
VALUES
('2025-01-24 17:08:11.311811', '2025-02-24 09:00:00.000000', 345269, 1, '630db5ad-344b-4dc1-9c5b-8066b5bbf046', '630db5ad-344b-4dc1-9c5b-8066b5bbf046_christmasTree.jpeg', 'image/jpeg', 'tester1001', 'https://sookyung-s3-bucket.s3.ap-northeast-2.amazonaws.com/630db5ad-344b-4dc1-9c5b-8066b5bbf046_christmasTree.jpeg', null, 'https://sookyung-s3-bucket-resized.s3.ap-northeast-2.amazonaws.com/resized-630db5ad-344b-4dc1-9c5b-8066b5bbf046_christmasTree.jpeg', 'PROFILE'),
('2025-01-24 17:10:36.406541', '2025-02-24 09:00:00.000000', 345269, 2, '96563c15-afda-44f7-a211-fd357bdba907', '96563c15-afda-44f7-a211-fd357bdba907_christmasTree.jpeg', 'image/jpeg', 'tester1001', 'https://sookyung-s3-bucket.s3.ap-northeast-2.amazonaws.com/96563c15-afda-44f7-a211-fd357bdba907_christmasTree.jpeg', '76caa884-dfc8-4ea1-b077-85263a24d8e7', 'https://sookyung-s3-bucket-resized.s3.ap-northeast-2.amazonaws.com/resized-96563c15-afda-44f7-a211-fd357bdba907_christmasTree.jpeg', 'CHAT');

--chatRoom
INSERT INTO chat_db.room (created_at, id, room_id, room_name, room_type)
VALUES
('2025-01-24 17:10:10.973500', 1, '76caa884-dfc8-4ea1-b077-85263a24d8e7', 'room1001', 'GM');

--participants
INSERT INTO chat_db.participants (chat_room_id, id, user_id, role)
VALUES
(1, 1, 7, 'OWNER'),
(1, 2, 6, 'MEMBER'),
(1, 3, 8, 'MEMBER');

--notification
INSERT INTO chat_db.notification (is_read, created_at, id, message, receiver, room_id, room_name, sender, room_type)
VALUES
(false, '2025-01-24 17:10:11.036201', 1, '새로운 채팅방에 초대되었습니다 : \'room1001\'', 'tester1001', '76caa884-dfc8-4ea1-b077-85263a24d8e7', 'room1001', 'tester1000', 'GM'),
(false, '2025-01-24 17:10:11.041345', 2, '새로운 채팅방에 초대되었습니다 : \'room1001\'', 'tester1002', '76caa884-dfc8-4ea1-b077-85263a24d8e7', 'room1001', 'tester1000', 'GM');
------------------------