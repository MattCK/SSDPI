CREATE TABLE IF NOT EXISTS `users` (
  `USR_id` smallint(5) unsigned NOT NULL auto_increment,
  `USR_username` varchar(16) collate utf8_unicode_ci NOT NULL,
  `USR_password` varchar(64) collate utf8_unicode_ci NOT NULL,
  `USR_firstName` varchar(24) collate utf8_unicode_ci NOT NULL,
  `USR_lastName` varchar(24) collate utf8_unicode_ci NOT NULL,
  `USR_email` varchar(64) collate utf8_unicode_ci NOT NULL,
  `USR_verified` tinyint(1) NOT NULL,
  `USR_tieBreaker` smallint(5) unsigned NOT NULL,
  `USR_transactionData` text collate utf8_unicode_ci,
  `USR_archived` tinyint(1) NOT NULL,
  `USR_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `USR_version` smallint(5) unsigned NOT NULL default '1',
  PRIMARY KEY  (`USR_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `_usershist` (
  `USR_id` smallint(5) unsigned NOT NULL,
  `USR_username` varchar(16) collate utf8_unicode_ci NOT NULL,
  `USR_password` varchar(64) collate utf8_unicode_ci NOT NULL,
  `USR_firstName` varchar(24) collate utf8_unicode_ci NOT NULL,
  `USR_lastName` varchar(24) collate utf8_unicode_ci NOT NULL,
  `USR_email` varchar(64) collate utf8_unicode_ci NOT NULL,
  `USR_tieBreaker` smallint(5) unsigned NOT NULL,
  `USR_transactionData` text collate utf8_unicode_ci,
  `USR_verified` tinyint(1) NOT NULL,
  `USR_archived` tinyint(1) NOT NULL,
  `USR_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `USR_version` smallint(5) unsigned NOT NULL,
  PRIMARY KEY  (`USR_id`,`USR_version`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Triggers `users`
--
DROP TRIGGER IF EXISTS `juiciobracket`.`usersInsertHist`;
DELIMITER //
CREATE TRIGGER `juiciobracket`.`usersInsertHist` AFTER INSERT ON `juiciobracket`.`users`
 FOR EACH ROW INSERT INTO 
 _usershist SELECT * FROM users WHERE USR_id = NEW.USR_id
//
DELIMITER ;
DROP TRIGGER IF EXISTS `juiciobracket`.`usersUpdateVersion`;
DELIMITER //
CREATE TRIGGER `juiciobracket`.`usersUpdateVersion` BEFORE UPDATE ON `juiciobracket`.`users`
 FOR EACH ROW BEGIN
	SET NEW.USR_version = OLD.USR_version + 1;
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `juiciobracket`.`usersUpdateHist`;
DELIMITER //
CREATE TRIGGER `juiciobracket`.`usersUpdateHist` AFTER UPDATE ON `juiciobracket`.`users`
 FOR EACH ROW INSERT INTO _usershist SELECT * FROM users WHERE USR_id = NEW.USR_id
//
DELIMITER ;

CREATE TABLE IF NOT EXISTS `userlogins` (
  `LGN_USR_id` smallint(5) unsigned NOT NULL,
  `LGN_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`LGN_USR_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
