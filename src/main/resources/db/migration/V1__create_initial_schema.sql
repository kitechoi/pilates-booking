CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    phone_number VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_member_member_number UNIQUE (member_number)
);

CREATE TABLE instructor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    profile_image_url VARCHAR,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE class_session (
    id BIGSERIAL PRIMARY KEY,
    instructor_id BIGINT NOT NULL,
    class_type VARCHAR NOT NULL,
    start_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    reservation_open_at TIMESTAMP NOT NULL,
    capacity INTEGER NOT NULL,
    reserved_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_class_session_instructor
        FOREIGN KEY (instructor_id) REFERENCES instructor (id)
);

CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    class_session_id BIGINT NOT NULL,
    status VARCHAR NOT NULL,
    reserved_at TIMESTAMP NOT NULL,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reservation_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_reservation_class_session
        FOREIGN KEY (class_session_id) REFERENCES class_session (id)
);

CREATE UNIQUE INDEX uk_reservation_active_member_class_session
    ON reservation (member_id, class_session_id)
    WHERE status = 'RESERVED';
