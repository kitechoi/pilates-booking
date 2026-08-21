ALTER TABLE class_session
    ADD CONSTRAINT ck_class_session_reserved_count
    CHECK (reserved_count >= 0 AND reserved_count <= capacity);
