--DROP COLUMN
ALTER TABLE project_project_template DROP COLUMN IF EXISTS invoicing_type_select;
ALTER TABLE base_app_project DROP COLUMN IF EXISTS generate_project_sequence;
ALTER TABLE project_project DROP COLUMN IF EXISTS status_select;
ALTER TABLE project_project DROP COLUMN IF EXISTS due_date;
ALTER TABLE project_project DROP COLUMN IF EXISTS estimated_time_days;
ALTER TABLE project_project DROP COLUMN IF EXISTS progress;
ALTER TABLE project_project DROP COLUMN IF EXISTS company;
ALTER TABLE project_project DROP COLUMN IF EXISTS is_project;
ALTER TABLE project_project DROP COLUMN IF EXISTS project_type_select;
ALTER TABLE project_project_status DROP COLUMN IF EXISTS default_status;
ALTER TABLE project_project_status DROP COLUMN IF EXISTS is_open;
ALTER TABLE project_project_status DROP COLUMN IF EXISTS is_close;

--DROP COLUMN M2M
DROP TABLE IF EXISTS businessproject_invoicing_project_project_set;
DROP TABLE IF EXISTS project_project_project_folder_set;
DROP TABLE IF EXISTS project_project_team_task_category_set;
DROP TABLE IF EXISTS project_project_template_project_folder_set;
DROP TABLE IF EXISTS team_task_category_set;

--MENU
DELETE FROM meta_menu_roles WHERE menus = (SELECT id FROM meta_menu WHERE name = 'project-phases');
DELETE FROM meta_menu_groups WHERE menus = (SELECT id FROM meta_menu WHERE name = 'project-phases');
DELETE FROM meta_menu WHERE name = 'project-phases';
DELETE FROM meta_action WHERE name = 'all.phases';
