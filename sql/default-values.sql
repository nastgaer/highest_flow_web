INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'members');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'tb_accounts');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'ranking');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'tblive');

INSERT INTO `tbl_members` (`id`, `member_name`, `password`, `level`, `salt`, `mobile`, `comment`, `type`, `created_time`, `updated_time`) VALUES
(1, 'administrator', '15d620a115e48c8abcff1c14a6eecc4ecf33bcf145d33030e7833574a4fe5a9c', 0, 'tDGUwFZhtUYVSRb2qgNd', '111-1111-1111', 'Administrator', 0, '2020-07-09 16:01:38', '2020-07-09 16:01:38');