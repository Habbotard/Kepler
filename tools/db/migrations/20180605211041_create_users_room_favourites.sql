-- migrate:up
CREATE TABLE `users_room_favourites` (
  `room_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- migrate:down
DROP TABLE `users_room_favourites`;
