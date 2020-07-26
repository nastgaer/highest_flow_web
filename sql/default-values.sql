INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'members');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'tb_accounts');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'ranking');
INSERT INTO `tbl_roles` (`id`, `name`) VALUES (NULL, 'tblive');

INSERT INTO `tbl_members` (`id`, `member_name`, `password`, `level`, `salt`, `mobile`, `comment`, `state`, `created_time`, `updated_time`) VALUES
(1, 'administrator', '15d620a115e48c8abcff1c14a6eecc4ecf33bcf145d33030e7833574a4fe5a9c', 99, 'tDGUwFZhtUYVSRb2qgNd', '111-1111-1111', 'Administrator', 0, '2020-07-09 16:01:38', '2020-07-09 16:01:38');

INSERT INTO `tbl_member_roles` (`id`, `member_id`, `role_id`, `created_time`) VALUES
(NULL, 1, 3, NULL),
(NULL, 1, 4, NULL),
(NULL, 1, 2, NULL),
(NULL, 1, 1, NULL);

INSERT INTO `tbl_codes` (`id`, `code_type`, `service_type`, `hours`, `code`, `state`, `machine_code`, `member_id`, `service_start_time`, `service_end_time`, `created_time`, `accepted_time`) VALUES
(NULL, 0, 0, 60, 'testeX5e53Df', 0, '', 0, NULL, NULL, now(), NULL);
