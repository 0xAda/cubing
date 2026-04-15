CREATE TABLE "user" (
    id INT NOT NULL GENERATED ALWAYS AS IDENTITY,
    wca_user_id INT NOT NULL,
    created_at timestamptz DEFAULT now(),
    CONSTRAINT user_id_pk PRIMARY KEY (id),
    CONSTRAINT wca_user_id_unique UNIQUE (wca_user_id)
);