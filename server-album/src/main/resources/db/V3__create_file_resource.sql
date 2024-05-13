ALTER TABLE storage_resource RENAME file_resource;
ALTER TABLE file_deletion_failed_history RENAME file_deletion_failed_log;

ALTER TABLE file_resource DROP COLUMN delete_failed_count;
ALTER TABLE file_resource RENAME COLUMN file_size to size;
