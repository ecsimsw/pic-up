ALTER TABLE album RENAME COLUMN resourceKey TO thumbnail;
ALTER TABLE album DROP COLUMN resourceFileSize;

ALTER TABLE album RENAME COLUMN resourceKey TO fileResource;
ALTER TABLE album RENAME COLUMN thumbnailResourceKey TO thumbnail;

