create SCHEMA IF NOT EXISTS portal;
create SCHEMA IF NOT EXISTS subscription;

drop table if exists subscription.user;
drop table if exists subscription.account;
drop table if exists subscription.userroles;
drop table if exists subscription.role;
drop table if exists subscription.productspecs;

create table subscription.account
(
    idAccount              int unsigned auto_increment
        primary key,
    idCustomer             int unsigned                    not null,
    accountName            varchar(128) default ''         not null,
    account_type           varchar(50)                     null,
    account_level          varchar(50)                     null,
    accessibleTier         varchar(20)                     null,
    apiId                  varchar(128)                    null,
    division               varchar(128)                    null,
    primaryContactName     varchar(45)  default ''         not null,
    primaryContactNumber   varchar(45)                     null,
    primaryContactEmail    varchar(125) default ''         not null,
    homeMarket             int                             null,
    status                 tinyint unsigned                not null,
    subscription_start     date                            null,
    access_level           varchar(20)  default 'Internal' null,
    access_level_percent   int                             null,
    historical_data_offset int          default 30         null
);

create table subscription.user
(
    idUser                int unsigned auto_increment
        primary key,
    idAccount             int unsigned            not null,
    firstName             varchar(50)             not null,
    lastName              varchar(50)             not null,
    emailAddress          varchar(125) default '' not null,
    password              varchar(65)  default '' not null,
    mobileNumber          varchar(45)             null,
    ddiNumber             varchar(45)             null,
    skypeId               varchar(45)             null,
    linkedIn              varchar(255)            null,
    createdOn             date                    null,
    status                tinyint unsigned        not null,
    loggedInBefore        tinyint unsigned        not null,
    title                 varchar(45)             null,
    department            varchar(45)             null,
    notificationReceived  tinyint      default 0  not null,
    lastLoggedInDate      date                    null,
    emailSent             tinyint      default 1  not null,
    jobTitle              varchar(255)            null,
    location              varchar(255)            null,
    region                varchar(255)            null,
    businessUnit          varchar(255)            null,
    arthurTouchPoint      varchar(255)            null,
    notes                 varchar(1000)           null,
    accessBreakdownModule tinyint(1)              null,
    freeVoucher           tinyint(1)   default 0  null,
    constraint fk_id_account_user
        foreign key (idAccount) references subscription.account (idAccount)
);

CREATE TABLE subscription.role (
  `idRole` int unsigned NOT NULL AUTO_INCREMENT,
  `roleName` varchar(125) NOT NULL DEFAULT '',
  `description` varchar(256) DEFAULT NULL,
  `status` tinyint unsigned NOT NULL,
  `idUser` int unsigned NOT NULL,
  `idAccount` int unsigned NOT NULL,
  `firstName` varchar(50) DEFAULT NULL,
  `lastName` varchar(50) DEFAULT NULL,
  `emailAddress` varchar(125) DEFAULT NULL,
  `password` varchar(65) DEFAULT NULL,
  `mobileNumber` varchar(45) DEFAULT NULL,
  `ddiNumber` varchar(45) DEFAULT NULL,
  `skypeId` varchar(45) DEFAULT NULL,
  `linkedIn` varchar(255) DEFAULT NULL,
  `createdOn` date DEFAULT NULL,
  `loggedInBefore` tinyint unsigned NOT NULL,
  `title` varchar(45) DEFAULT NULL,
  `department` varchar(45) DEFAULT NULL,
  `notificationReceived` tinyint NOT NULL,
  `lastLoggedInDate` date DEFAULT NULL,
  `emailSent` tinyint NOT NULL,
  `jobTitle` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `businessUnit` varchar(255) DEFAULT NULL,
  `arthurTouchPoint` varchar(255) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `accessBreakdownModule` tinyint(1) DEFAULT NULL,
  `freeVoucher` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`idRole`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb3;


CREATE TABLE subscription.product (
  `idProduct` int unsigned NOT NULL,
  `name` varchar(45) NOT NULL DEFAULT '',
  `description` varchar(256) DEFAULT NULL,
  `access_type` varchar(20) DEFAULT 'account_level',
  `status` tinyint unsigned NOT NULL,
  PRIMARY KEY (`idProduct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE subscription.userroles (
  `iduserRoles` int unsigned NOT NULL AUTO_INCREMENT,
  `idUser` int unsigned NOT NULL,
  `idRole` int unsigned NOT NULL,
  PRIMARY KEY (`iduserRoles`),
  KEY `idUser_idx` (`idUser`),
  KEY `idRole_idx` (`idRole`),
  CONSTRAINT `fk_roleid_userrole` FOREIGN KEY (`idRole`) REFERENCES `role` (`idRole`) ON UPDATE CASCADE,
  CONSTRAINT `fk_userid_userrole` FOREIGN KEY (`idUser`) REFERENCES `user` (`idUser`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4112 DEFAULT CHARSET=utf8mb3;

CREATE TABLE subscription.productspecs (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `idAccount` int unsigned NOT NULL,
  `idProduct` int unsigned NOT NULL,
  `specificationName` varchar(45) DEFAULT 'na',
  `specificationValue` varchar(256) DEFAULT 'na',
  PRIMARY KEY (`id`),
  KEY `fk_id_account_specs` (`idAccount`),
  KEY `fk_id_product_specs` (`idProduct`),
  KEY `idx_accountId_and_productId` (`idAccount`,`idProduct`) USING BTREE,
  CONSTRAINT `fk_id_account_specs` FOREIGN KEY (`idAccount`) REFERENCES `account` (`idAccount`),
  CONSTRAINT `fk_id_product_specs` FOREIGN KEY (`idProduct`) REFERENCES `product` (`idProduct`)
) ENGINE=InnoDB AUTO_INCREMENT=990 DEFAULT CHARSET=utf8mb3;

CREATE TABLE subscription.`product_permissions` (
  `permissionId` int unsigned NOT NULL AUTO_INCREMENT,
  `productId` int unsigned NOT NULL COMMENT 'FK : product.idProduct',
  `permission` varchar(100) NOT NULL DEFAULT '' COMMENT 'Name of the permission',
  `description` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'Display friendly description',
  `isEnabledAtAccountLevel` enum('user_level','account_level') NOT NULL DEFAULT 'user_level' COMMENT 'Whether the permission is applicable at account level (by default user level only)',
  PRIMARY KEY (`permissionId`),
  KEY `productId` (`productId`),
  CONSTRAINT `product_permissions_FK` FOREIGN KEY (`productId`) REFERENCES `product` (`idProduct`)
);

CREATE TABLE subscription.`user_permissions` (
  `userPermissionId` int unsigned NOT NULL AUTO_INCREMENT,
  `userId` int unsigned NOT NULL,
  `permissionId` int unsigned NOT NULL,
  PRIMARY KEY (`userPermissionId`),
  UNIQUE KEY `idx_userId_permissionId` (`userId`,`permissionId`),
  KEY `user_permissions_ibfk_2` (`permissionId`),
  CONSTRAINT `user_permissions_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`idUser`),
  CONSTRAINT `user_permissions_ibfk_2` FOREIGN KEY (`permissionId`) REFERENCES `product_permissions` (`permissionId`)
);

INSERT INTO subscription.role (idRole,roleName,description,status,idUser,idAccount,firstName,lastName,emailAddress,password,mobileNumber,ddiNumber,skypeId,linkedIn,createdOn,loggedInBefore,title,department,notificationReceived,lastLoggedInDate,emailSent,jobTitle,location,region,businessUnit,arthurTouchPoint,notes,accessBreakdownModule,freeVoucher) VALUES
	 (1,'Portal Admin','Portal Admin',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (2,'Portal Viewer','Base Role for any Portal User',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (3,'Helpdesk Viewer','Helpdesk Viewer',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (4,'Helpdesk Admin','This role allows an access from helpdesk api',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (5,'Support Account','Used for account support',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (6,'arthur Admin','Role for arthur Admin Features like Resources, Notifications etc..',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (7,'Helpdesk Title Creator','Role to allow the user to view and approve/reject title creation requests',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (8,'Brand Affinity Access','Has access to create reports using the brand affinity API',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (9,'Helpdesk Title Name Change','Has the ability to change the name of a title',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (10,'Monitor Viewer','Base Role for any Monitor Viewer',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO subscription.role (idRole,roleName,description,status,idUser,idAccount,firstName,lastName,emailAddress,password,mobileNumber,ddiNumber,skypeId,linkedIn,createdOn,loggedInBefore,title,department,notificationReceived,lastLoggedInDate,emailSent,jobTitle,location,region,businessUnit,arthurTouchPoint,notes,accessBreakdownModule,freeVoucher) VALUES
	 (11,'Enterprise Viewer','Base Role for any Enterprise Viewer',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (12,'Enterprise Admin','Admin Role for any Enterprise',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (13,'Helpdesk Account Management','Role for giving user permissions for helpdesk',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (14,'Talent Lite Viewer','Access to the Lite users to view sneak peek of Talent module',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (15,'Talent Enterprise Module','Full Access to Talent module',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (16,'Movie Lite Viewer','Access to the Lite users to view sneak peek of Movie module',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (17,'Movie Enterprise Module','Full Access to Movie module',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (100,'External API','Allow the third-party system to program access to TV-Show data from external',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (110,'Movie Custom Report Access','A custom access role to determine whenever a user be able to export the report',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
	 (111,'Audience Insights Module Access','A custom access role to determine whenever an user can access audience insights data',1,0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);


INSERT INTO subscription.product (idProduct,name,description,access_type,status) VALUES
	 (1,'Demand Portal','Subscription to TV Enterprise Module','account_level',1),
	 (2,'Historical Data Access','Historical data access product','account_level',1),
	 (3,'Global Trends','Full Access to Global Trends','account_level',1),
	 (4,'TopContent Module','Visibility to top n titles across an accounts subcription markets','account_level',1),
	 (5,'Demand Breakdown Module','Visibility to the Demand Breakdown by Demand Source Type Categories','user_level',1),
	 (6,'External API','API Access to TVShow Data','account_level',1),
	 (7,'Talent Enterprise Module','Full Access to Talent Demand','account_level',1),
	 (8,'Talent Lite Viewer','Visibility to Talent Lite Version','account_level',1),
	 (9,'Movie Enterprise Module','Full Access to Movie Demand','account_level',1),
	 (10,'Movie Lite Viewer','Visibility to Movie Lite Version','account_level',1);

INSERT INTO subscription.product (idProduct,name,description,access_type,status) VALUES
	 (11,'Movie Custom Report Module','Allow User Access to Exporting Movie data','account_level',1),
	 (12,'Audience Insights Module','Allow User Access to Audience Insights data','account_level',1);

INSERT INTO subscription.product (idProduct,name,description,access_type,status) VALUES
 (13,'CMS','Content Management Service','user_level',1),
 (14,'TV Demand API & Custom Data Export','Allow user to access TV Demand data via API and custom data exports','account_level',1),
 (15,'Movie Demand API & Custom Data Export','Allow user to access Movie Demand data via API and custom data exports','account_level',1),
 (16,'Talent Demand API & Custom Data Export','Allow user to access Talent Demand data via API and custom data exports','account_level',1);


insert into subscription.account (idAccount, idCustomer, accountName, account_type, account_level,
                                  accessibleTier, apiId, division, primaryContactName,
                                  primaryContactNumber, primaryContactEmail, homeMarket, status,
                                  subscription_start, access_level, access_level_percent,
                                  historical_data_offset)
values (100, 1, 'arthur Analytics (Sales)', 'subscription', 'allaccess', 'Enterprise', null, null,
        '', null, 'contact@arthuranalytcis.com', 226, 1, '2018-01-01', 'Advanced', 80, 0);

INSERT INTO subscription.account (idAccount, idCustomer, accountName, account_type, account_level,
                                  accessibleTier, apiId, division, primaryContactName,
                                  primaryContactNumber, primaryContactEmail, homeMarket, status,
                                  subscription_start, access_level, access_level_percent, historical_data_offset)
VALUES (1085, 54, 'TestApiKey', 'subscription', 'allaccess', 'Enterprise', '52252321-0f2a-4a5b-891a-cc47f8d2f39f',
       NULL, 'Test User', NULL, 'test.user@arthur.com', 226, 1, '2021-08-17', NULL, NULL, 32);

insert into subscription.user (idUser, idAccount, firstName, lastName, emailAddress, password,
                               mobileNumber, ddiNumber, skypeId, linkedIn, createdOn, status,
                               loggedInBefore, title, department, notificationReceived,
                               lastLoggedInDate, emailSent, jobTitle, location, region,
                               businessUnit, arthurTouchPoint, notes, accessBreakdownModule,
                               freeVoucher)
values (2089, 100, 'TestFirstName', 'TestLastName', 'test@arthur.com',
        '$2a$10$kcJRzkuS0/2D.nawpYUN6O6TypoI9Il2RI5XO4EhnngiW1WtDw3Ly', null, null, null, null,
        '2022-04-07', 1, 1, '', '', 1, '2022-07-29', 0, null, null, null, null, null, null, null,
        0);

INSERT INTO subscription.`user` (idUser, idAccount, firstName, lastName, emailAddress, password, mobileNumber,
                                 ddiNumber, skypeId, linkedIn, createdOn, status, loggedInBefore, title, department,
                                 notificationReceived, lastLoggedInDate, emailSent, jobTitle, location, region, businessUnit,
                                 arthurTouchPoint, notes, accessBreakdownModule, freeVoucher)
VALUES( 2389, 1085, 'API', 'User', 'apiuser@test.com', '', NULL, NULL, NULL, NULL, NULL, 1, 0, NULL, NULL, 1, NULL, 1, NULL, NULL,
 NULL, NULL, NULL, NULL, NULL, 0);

insert into subscription.userroles (iduserRoles, idUser, idRole) VALUE (1, 2089, 1);
insert into subscription.userroles (iduserRoles, idUser, idRole) VALUE (2, 2389, 1);
insert into subscription.productspecs (id, idAccount, idProduct, specificationName, specificationValue) VALUE (1, 100, 1, 'titles', '5000');
insert into subscription.productspecs (id, idAccount, idProduct) VALUE (2, 100, 9);
insert into subscription.productspecs (id, idAccount, idProduct) VALUE (3, 1085, 9);

INSERT INTO subscription.product_permissions (permissionId,productId,permission,description,isEnabledAtAccountLevel) VALUES
	 (1,13,'CMS:Login','A user is authorized to access/login to CMS','user_level'),
	 (2,13,'CMS:PlatformVerification:Search','The user can search for platform entities to be verified','account_level'),
	 (3,13,'CMS:PlatformVerification:Update','The user can update the platform verification status','account_level'),
	 (4,16,'D360:Talent:DemandDataAPIAndDataExport','The user can access talent demand through customer API','user_level'),
	 (5,14,'D360:TV:DemandDataAPIAndDataExport','The user can access show demand through customer API','user_level'),
	 (6,15,'D360:Movie:DemandDataAPIAndDataExport','The user can access movie demand through customer API','user_level');

INSERT INTO subscription.user_permissions (userId, permissionId) VALUES
    (2389,4),
    (2389,5),
    (2389,6);



