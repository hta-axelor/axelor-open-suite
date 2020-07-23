--Drop Column
ALTER TABLE project_project_template DROP COLUMN IF EXISTS invoicing_type_select;
ALTER TABLE base_app_project DROP COLUMN IF EXISTS generate_project_sequence;
ALTER TABLE project_project 
DROP COLUMN IF EXISTS due_date,
DROP COLUMN IF EXISTS estimated_time_days,
DROP COLUMN IF EXISTS progress,
DROP COLUMN IF EXISTS company,
DROP COLUMN IF EXISTS is_project,
DROP COLUMN IF EXISTS project_type_select;
ALTER TABLE project_project_status 
DROP COLUMN IF EXISTS default_status,
DROP COLUMN IF EXISTS is_open,
DROP COLUMN IF EXISTS is_close;

--Drop Column M2M
DROP TABLE IF EXISTS businessproject_invoicing_project_project_set;
DROP TABLE IF EXISTS project_project_project_folder_set;
DROP TABLE IF EXISTS project_project_team_task_category_set;
DROP TABLE IF EXISTS project_project_template_project_folder_set;
DROP TABLE IF EXISTS team_task_category_set;

--Menu
DELETE FROM meta_menu_roles WHERE menus = (SELECT id FROM meta_menu WHERE name = 'project-phases');
DELETE FROM meta_menu_groups WHERE menus = (SELECT id FROM meta_menu WHERE name = 'project-phases');
DELETE FROM meta_menu WHERE name = 'project-phases';
DELETE FROM meta_action WHERE name = 'all.phases';

DROP TABLE IF EXISTS project_project_status;

--Add status
CREATE TABLE project_project_status (
    id bigint NOT NULL,
    archived boolean,
    import_id character varying(255),
    import_origin character varying(255),
    version integer,
    created_on timestamp without time zone,
    updated_on timestamp without time zone,
    attrs jsonb,
    name character varying(255),
    created_by bigint,
    updated_by bigint,
    is_completed boolean,
    is_default_completed boolean,
    related_to_select integer,
    sequence integer
);

ALTER TABLE ONLY project_project_status
    ADD CONSTRAINT project_project_status_pkey PRIMARY KEY (id);    
ALTER TABLE ONLY project_project_status
    ADD CONSTRAINT uk_dmgpps0ek6jm6glvxpimcie1y UNIQUE (import_id);
    
CREATE INDEX project_project_status_name_idx ON project_project_status USING btree (name);

ALTER TABLE ONLY project_project_status
    ADD CONSTRAINT fk_b15qj3ui06sf6k2rfcf7trufg FOREIGN KEY (created_by) REFERENCES auth_user(id);
ALTER TABLE ONLY project_project_status
    ADD CONSTRAINT fk_i427yu6ube9y77urxxsbedgve FOREIGN KEY (updated_by) REFERENCES auth_user(id);

--Insert demo data values
INSERT INTO  project_project_status (id, import_id, version, created_on, name, created_by, is_completed, is_default_completed, related_to_select, sequence) VALUES
(1, 1, 0, now(), 'New', 1,false ,false, 1, 1)
,(2, 2, 0, now(), 'In progress', 1,false ,false, 1, 2)
,(3, 3, 0, now(), 'Done', 1,true ,true, 1, 3)
,(4, 4, 0, now(), 'Canceled', 1,true ,false, 1, 4)
,(5, 5, 0, now(), 'New', 1,false ,false, 2, 1)
,(6, 6, 0, now(), 'In progress', 1,false ,false, 2, 2)
,(7, 7, 0, now(), 'Done', 1,true ,true, 2, 3)
,(8, 8, 0, now(), 'Canceled', 1,true ,false, 2, 4);

--Add Priority
CREATE TABLE project_project_priority (
    id bigint NOT NULL,
    archived boolean,
    import_id character varying(255),
    import_origin character varying(255),
    version integer,
    created_on timestamp without time zone,
    updated_on timestamp without time zone,
    attrs jsonb,
    name character varying(255),
    technical_type_select integer,
    created_by bigint,
    updated_by bigint
);

ALTER TABLE ONLY project_project_priority
    ADD CONSTRAINT project_project_priority_pkey PRIMARY KEY (id);    
ALTER TABLE ONLY project_project_priority
    ADD CONSTRAINT uk_9oi37l1d33q48fhph1yas715i UNIQUE (import_id);
    
CREATE INDEX project_project_priority_name_idx ON project_project_priority USING btree (name);

ALTER TABLE ONLY project_project_priority
    ADD CONSTRAINT fk_opqj5vqqcbfygjsylo5sx3f20 FOREIGN KEY (created_by) REFERENCES auth_user(id);
ALTER TABLE ONLY project_project_priority
    ADD CONSTRAINT fk_6ftbvrmtb8yg4s40fced7u2ry FOREIGN KEY (updated_by) REFERENCES auth_user(id);

--Insert demo data values
INSERT INTO  project_project_priority (id, import_id, version, created_on, name, technical_type_select, created_by) VALUES
(1, 1, 0, now(), 'Low', 1 , 1)
,(2, 2, 0, now(), 'Normal', 2 , 1)
,(3, 3, 0, now(), 'High', 3 , 1)
,(4, 4, 0, now(), 'Urgent', 4 , 1);

--Add status for project
ALTER TABLE project_project ADD COLUMN project_status BIGINT;

CREATE INDEX project_project_project_status_idx ON project_project USING btree (project_status);

ALTER TABLE ONLY project_project
    ADD CONSTRAINT fk_b277b4wq3mdcshg7j66q4nhr5 FOREIGN KEY (project_status) REFERENCES project_project_status(id);

UPDATE project_project AS project 
	SET project_status = 
		(SELECT id FROM project_project_status status 
						WHERE status.id = project.status_select);

ALTER TABLE project_project DROP COLUMN IF EXISTS status_select;

--Switch M2M boolean to true
ALTER TABLE project_project ADD COLUMN is_show_status boolean;
ALTER TABLE project_project ADD COLUMN is_show_priority boolean;
UPDATE project_project SET  is_show_status = true;
UPDATE project_project SET  is_show_priority = true;

--Add status M2M
CREATE TABLE project_project_team_task_status_set (
project_project bigint NOT NULL,
team_task_status_set bigint NOT NULL
);

CREATE INDEX project_project_team_task_status_set_pkey ON project_project_team_task_status_set USING btree (project_project, team_task_status_set);

--Insert demo data values
INSERT INTO project_project_team_task_status_set (project_project, team_task_status_set)
SELECT id,5
FROM project_project;
INSERT INTO project_project_team_task_status_set (project_project, team_task_status_set)
SELECT id,6
FROM project_project;
INSERT INTO project_project_team_task_status_set (project_project, team_task_status_set)
SELECT id,7
FROM project_project;
INSERT INTO project_project_team_task_status_set (project_project, team_task_status_set)
SELECT id,8
FROM project_project;

--Add priority M2M
CREATE TABLE project_project_team_task_priority_set (
project_project bigint NOT NULL,
team_task_priority_set bigint NOT NULL
);

CREATE INDEX project_project_team_task_priority_set_pkey ON project_project_team_task_priority_set USING btree (project_project, team_task_priority_set);

--Insert demo data values
INSERT INTO project_project_team_task_priority_set (project_project, team_task_priority_set)
SELECT id,1
FROM project_project;
INSERT INTO project_project_team_task_priority_set (project_project, team_task_priority_set)
SELECT id,2
FROM project_project;
INSERT INTO project_project_team_task_priority_set (project_project, team_task_priority_set)
SELECT id,3
FROM project_project;
INSERT INTO project_project_team_task_priority_set (project_project, team_task_priority_set)
SELECT id,4
FROM project_project;

--Add status for task
ALTER TABLE team_task ADD COLUMN task_status BIGINT;

CREATE INDEX team_task_task_status_idx ON team_task USING btree (task_status);

ALTER TABLE ONLY team_task
    ADD CONSTRAINT fk_17b2mgvaperiqyfx0nb7il9rm FOREIGN KEY (task_status) REFERENCES project_project_status(id);

UPDATE team_task AS task 
	SET task_status = 
		(SELECT id FROM project_project_status status 
						WHERE LOWER(status.name) = LOWER(task.status) AND related_to_select = 2);

UPDATE team_task AS task SET task_status = 6 WHERE status = 'in-progress';
UPDATE team_task AS task SET task_status = 7 WHERE status = 'closed';

--Add priority for task
ALTER TABLE team_task ADD COLUMN task_priority BIGINT;

CREATE INDEX team_task_task_priority_idx ON team_task USING btree (task_priority);

ALTER TABLE ONLY team_task
    ADD CONSTRAINT fk_2ss9tf31cebrvof7hdagm7amt FOREIGN KEY (task_priority) REFERENCES project_project_priority(id);

UPDATE team_task AS task 
	SET task_priority = 
		(SELECT id FROM project_project_priority priority 
						WHERE LOWER(priority.name) = LOWER(task.priority));
