INSERT INTO url
  (name, created_at)
VALUES
  ('https://github.com', '2023-07-23 11:00:00'),
  ('https://github.commm', '2023-07-23 11:00:00'),
  ('https://ya.ru', '2023-07-23 12:00:00');

  INSERT INTO url_check
    (status_code, title, h1, description, url_id, created_at)
  VALUES
    (200, 'Яндекс', 'Все сервисы', '', 3, '2023-07-23 12:01:00');
