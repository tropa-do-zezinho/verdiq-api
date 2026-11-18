-- Seed Verdiq (Oracle). Senha demo: Senha@123

INSERT INTO usuario (id, nome, email, telefone, senha_hash, papel, cidade, uf, consentimento_notificacoes, consentimento_comparativos)
VALUES
 (1, 'Admin Verdiq', 'admin@naepoca.local', NULL, '$2b$10$kU6XjwQBF/NDii4XTZAY5O.Wrw48vQih8PTfOxb/50XkCbVqQ6dv6', 'ADMIN', 'São Paulo', 'SP', 1, 1);
INSERT INTO usuario (id, nome, email, telefone, senha_hash, papel, cidade, uf, consentimento_notificacoes, consentimento_comparativos)
VALUES
 (2, 'Marta Feirante', 'marta@naepoca.local', '11999990001', '$2b$10$kU6XjwQBF/NDii4XTZAY5O.Wrw48vQih8PTfOxb/50XkCbVqQ6dv6', 'COMERCIANTE', 'São Paulo', 'SP', 1, 1);
INSERT INTO usuario (id, nome, email, telefone, senha_hash, papel, cidade, uf, consentimento_notificacoes, consentimento_comparativos)
VALUES
 (3, 'Ana Cliente', 'ana@naepoca.local', '11999990002', '$2b$10$kU6XjwQBF/NDii4XTZAY5O.Wrw48vQih8PTfOxb/50XkCbVqQ6dv6', 'CLIENTE', 'São Paulo', 'SP', 1, 0);

INSERT INTO praca (id, codigo, nome, cidade, uf, fonte) VALUES
 (1, 'CEAGESP_SP', 'CEAGESP São Paulo', 'São Paulo', 'SP', 'PROHORT');
INSERT INTO praca (id, codigo, nome, cidade, uf, fonte) VALUES
 (2, 'CEASA_BH', 'CEASA Minas Belo Horizonte', 'Belo Horizonte', 'MG', 'PROHORT');

INSERT INTO comerciante (id, usuario_id, nome_fantasia, tipo_negocio, praca_id, plano, latitude, longitude)
VALUES (1, 2, 'Hortifruti da Marta', 'FEIRA', 1, 'PRO', -23.5505, -46.6333);

INSERT INTO cliente_perfil (id, usuario_id, cidade, uf) VALUES (1, 3, 'São Paulo', 'SP');

INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (1, 'Limão Taiti', 'FRUTA', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (2, 'Morango', 'FRUTA', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (3, 'Tomate', 'LEGUME', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (4, 'Banana Nanica', 'FRUTA', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (5, 'Alface Crespa', 'VERDURA', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (6, 'Cebola', 'LEGUME', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (7, 'Batata', 'LEGUME', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (8, 'Cenoura', 'LEGUME', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (9, 'Laranja Pera', 'FRUTA', 'KG');
INSERT INTO produto (id, nome_canonico, categoria, unidade_base) VALUES (10, 'Abacaxi', 'FRUTA', 'UN');

INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (1, 'limao');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (1, 'limão');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (1, 'tahiti');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (2, 'morango');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (3, 'tomate salada');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (4, 'banana');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (5, 'alface');
INSERT INTO produto_sinonimo (produto_id, sinonimo) VALUES (9, 'laranja');

INSERT INTO produto_substituto (produto_id, substituto_id) VALUES (1, 9);
INSERT INTO produto_substituto (produto_id, substituto_id) VALUES (2, 4);
INSERT INTO produto_substituto (produto_id, substituto_id) VALUES (3, 8);
INSERT INTO produto_substituto (produto_id, substituto_id) VALUES (5, 8);

INSERT INTO cliente_interesse (cliente_perfil_id, produto_id) VALUES (1, 1);
INSERT INTO cliente_interesse (cliente_perfil_id, produto_id) VALUES (1, 2);
INSERT INTO cliente_interesse (cliente_perfil_id, produto_id) VALUES (1, 3);
INSERT INTO comerciante_produto_principal (comerciante_id, produto_id) VALUES (1, 1);
INSERT INTO comerciante_produto_principal (comerciante_id, produto_id) VALUES (1, 2);
INSERT INTO comerciante_produto_principal (comerciante_id, produto_id) VALUES (1, 3);
INSERT INTO comerciante_produto_principal (comerciante_id, produto_id) VALUES (1, 4);

INSERT INTO plano (codigo, nome, preco_mensal, beneficios, ativo) VALUES
 ('ESSENCIAL', 'Comerciante Essencial', 0.00, 'Calendário de safra, registro de vendas/perdas, alertas básicos, painel simples', 1);
INSERT INTO plano (codigo, nome, preco_mensal, beneficios, ativo) VALUES
 ('PRO', 'Comerciante Pro', 49.90, 'Recomendações personalizadas, importação de planilha, comparativo com a praça, simuladores', 1);

INSERT INTO preferencia_notificacao (usuario_id, canal_web_push, canal_in_app, max_por_semana)
VALUES (2, 0, 1, 3);
INSERT INTO preferencia_notificacao (usuario_id, canal_web_push, canal_in_app, max_por_semana)
VALUES (3, 0, 1, 3);

INSERT INTO evento_sazonal (id, nome, regra_data, mes_tipico, descricao) VALUES
 (1, 'Carnaval', 'mobile-fevereiro', 2, 'Pico de limão e caipirinha');
INSERT INTO evento_sazonal (id, nome, regra_data, mes_tipico, descricao) VALUES
 (2, 'Festas Juninas', 'fixo-julho', 6, 'Morango, milho e doces');
INSERT INTO evento_sazonal (id, nome, regra_data, mes_tipico, descricao) VALUES
 (3, 'Natal', 'dezembro', 12, 'Demanda alta de frutas de mesa');
INSERT INTO evento_sazonal (id, nome, regra_data, mes_tipico, descricao) VALUES
 (4, 'Páscoa', 'marco-abril', 3, 'Demanda de frutas e ovos');
INSERT INTO evento_sazonal (id, nome, regra_data, mes_tipico, descricao) VALUES
 (5, 'Volta às aulas', 'fevereiro', 2, 'Ajuste de consumo familiar');

INSERT INTO evento_produto (evento_id, produto_id) VALUES (1, 1);
INSERT INTO evento_produto (evento_id, produto_id) VALUES (2, 2);
INSERT INTO evento_produto (evento_id, produto_id) VALUES (3, 4);
INSERT INTO evento_produto (evento_id, produto_id) VALUES (3, 9);
INSERT INTO evento_produto (evento_id, produto_id) VALUES (4, 2);
INSERT INTO evento_produto (evento_id, produto_id) VALUES (5, 5);

-- IPCA mensal (48 meses a partir de 2022-01)
INSERT INTO ipca_mensal (ano_mes, indice)
SELECT TO_CHAR(ADD_MONTHS(DATE '2022-01-01', LEVEL - 1), 'YYYY-MM'),
       ROUND(100 + (LEVEL - 1) * 0.35, 4)
FROM DUAL
CONNECT BY LEVEL <= 48;

-- Preços semanais sintetizados (produtos 1-8)
INSERT INTO preco_mercado (produto_id, praca_id, data_ref, preco_medio, volume_kg, fonte)
SELECT p.id, 1, DATE '2022-01-01' + (w.n * 7),
       CASE p.id
         WHEN 1 THEN 4.5 + 1.5 * SIN((EXTRACT(MONTH FROM DATE '2022-01-01' + (w.n * 7)) - 1) * ACOS(-1) / 6)
         WHEN 2 THEN 18.0 + 6.0 * COS((EXTRACT(MONTH FROM DATE '2022-01-01' + (w.n * 7)) - 7) * ACOS(-1) / 6)
         WHEN 3 THEN 5.0 + 1.2 * SIN(EXTRACT(MONTH FROM DATE '2022-01-01' + (w.n * 7)))
         WHEN 4 THEN 3.2 + 0.8 * SIN(EXTRACT(MONTH FROM DATE '2022-01-01' + (w.n * 7)) / 2)
         ELSE 4.0 + 0.5 * SIN(EXTRACT(MONTH FROM DATE '2022-01-01' + (w.n * 7)))
       END,
       1000 + 200 * SIN(w.n / 20.0),
       'SEED'
FROM (SELECT LEVEL - 1 AS n FROM DUAL CONNECT BY LEVEL <= 200) w
CROSS JOIN (SELECT id FROM produto WHERE id BETWEEN 1 AND 8) p;

-- Índices sazonais PRECO / OFERTA
INSERT INTO indice_sazonal (produto_id, praca_id, comerciante_id, mes, tipo, valor, anos_usados, confianca)
SELECT p.id, 1, NULL, m.mes, 'PRECO',
       CASE
         WHEN p.id = 1 AND m.mes = 1 THEN 0.82
         WHEN p.id = 1 AND m.mes = 2 THEN 0.95
         WHEN p.id = 2 AND m.mes = 7 THEN 0.78
         WHEN p.id = 2 AND m.mes IN (11, 12) THEN 1.25
         ELSE 0.9 + 0.05 * SIN(m.mes)
       END,
       36, 'ALTA'
FROM produto p
CROSS JOIN (SELECT LEVEL AS mes FROM DUAL CONNECT BY LEVEL <= 12) m
WHERE p.id BETWEEN 1 AND 8;

INSERT INTO indice_sazonal (produto_id, praca_id, comerciante_id, mes, tipo, valor, anos_usados, confianca)
SELECT p.id, 1, NULL, m.mes, 'OFERTA',
       CASE
         WHEN p.id = 1 AND m.mes = 1 THEN 1.12
         WHEN p.id = 2 AND m.mes = 7 THEN 1.20
         ELSE 1.0 + 0.04 * COS(m.mes)
       END,
       36, 'ALTA'
FROM produto p
CROSS JOIN (SELECT LEVEL AS mes FROM DUAL CONNECT BY LEVEL <= 12) m
WHERE p.id BETWEEN 1 AND 8;

INSERT INTO indice_sazonal (produto_id, praca_id, comerciante_id, mes, tipo, valor, anos_usados, confianca)
SELECT 1, 1, 1, m.mes, 'VENDAS',
       CASE WHEN m.mes = 2 THEN 1.45 WHEN m.mes = 1 THEN 1.10 ELSE 0.95 END,
       24, 'MEDIA'
FROM (SELECT LEVEL AS mes FROM DUAL CONNECT BY LEVEL <= 12) m;

INSERT INTO lote_envio (id, comerciante_id, tipo, origem, qtd_linhas)
VALUES (1, 1, 'COMPRA', 'MANUAL', 2);
INSERT INTO lote_envio (id, comerciante_id, tipo, origem, qtd_linhas)
VALUES (2, 1, 'VENDA', 'MANUAL', 2);
INSERT INTO lote_envio (id, comerciante_id, tipo, origem, qtd_linhas)
VALUES (3, 1, 'PERDA', 'MANUAL', 1);

INSERT INTO compra (comerciante_id, produto_id, lote_envio_id, kg, valor_total, valor_unitario_envio, data_ref, fornecedor)
VALUES (1, 1, 1, 50, 200.00, 4.00, CURRENT_DATE - 10, 'Ceagesp');
INSERT INTO compra (comerciante_id, produto_id, lote_envio_id, kg, valor_total, valor_unitario_envio, data_ref, fornecedor)
VALUES (1, 2, 1, 20, 360.00, 18.00, CURRENT_DATE - 8, 'Ceagesp');

INSERT INTO venda (comerciante_id, produto_id, lote_envio_id, kg, valor_total, valor_unitario_envio, data_ref)
VALUES (1, 1, 2, 40, 280.00, 7.00, CURRENT_DATE - 5);
INSERT INTO venda (comerciante_id, produto_id, lote_envio_id, kg, valor_total, valor_unitario_envio, data_ref)
VALUES (1, 2, 2, 15, 375.00, 25.00, CURRENT_DATE - 3);

INSERT INTO perda (comerciante_id, produto_id, lote_envio_id, kg, motivo, valor_estimado_envio, data_ref)
VALUES (1, 2, 3, 2.5, 'ESTRAGOU', 45.00, CURRENT_DATE - 2);

INSERT INTO oferta (comerciante_id, produto_id, preco_normal, preco_promocional, quantidade_kg, inicio, fim, status, ultima_chance)
VALUES (1, 2, 28.00, 19.90, 10, CURRENT_DATE, CURRENT_DATE + 5, 'ATIVA', 1);

INSERT INTO recomendacao (comerciante_id, produto_id, tipo, titulo, mensagem, confianca, dados_json, periodo_inicio, periodo_fim, fonte)
VALUES (
 1, 1, 'JANELA_COMPRA', 'Boa época para comprar limão',
 'Janeiro costuma ser o mês mais barato para limão. Seu pico de vendas é em fevereiro: compre agora.',
 'ALTA',
 '{"is_preco":0.82,"is_oferta":1.12,"is_vendas_fev":1.45,"anos":36}',
 TRUNC(CURRENT_DATE, 'MM'),
 LAST_DAY(CURRENT_DATE),
 'SEED/PROHORT'
);

INSERT INTO recomendacao (comerciante_id, produto_id, tipo, titulo, mensagem, confianca, dados_json, periodo_inicio, periodo_fim, fonte)
VALUES (
 NULL, 2, 'EM_SAFRA', 'Morango em época',
 'Morango com preço abaixo da média e oferta elevada na região de São Paulo.',
 'ALTA',
 '{"is_preco":0.78,"is_oferta":1.20,"praca":"CEAGESP_SP"}',
 CURRENT_DATE - 15, CURRENT_DATE + 15, 'SEED'
);

INSERT INTO notificacao (usuario_id, titulo, corpo, tipo)
VALUES (2, 'Janela de compra: Limão Taiti', 'Boa época para reforçar o estoque de limão.', 'JANELA_COMPRA');

INSERT INTO etl_run (fonte, status, finalizado_em, registros, mensagem)
VALUES ('SEED', 'SUCCESS', CURRENT_TIMESTAMP, 1, 'Carga inicial de dados base');
