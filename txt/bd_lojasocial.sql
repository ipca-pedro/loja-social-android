-- Ativa a extensão para gerar IDs únicos (UUIDs), comum em plataformas como Supabase
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- RF1: Autenticação e Controlo de Acessos
-- Tabela para os colaboradores dos SAS que vão usar a app Android
CREATE TABLE IF NOT EXISTS colaboradores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- Guardar sempre a hash, nunca a password
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- RF2: Gestão de Beneficiários
-- Tabela para os estudantes que recebem apoio
CREATE TABLE IF NOT EXISTS beneficiarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome_completo VARCHAR(255) NOT NULL,
    num_estudante VARCHAR(50) UNIQUE,
    nif VARCHAR(9) UNIQUE,
    ano_curricular INT,
    curso VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    telefone VARCHAR(20),
    notas_adicionais TEXT, -- Para alergias, restrições, etc.
    estado VARCHAR(50) NOT NULL DEFAULT 'ativo', -- 'ativo', 'inativo' (para reativação)
    data_registo TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- RF5: Gestão de Campanhas
-- Tabela para registar campanhas de doação
CREATE TABLE IF NOT EXISTS campanhas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE
);

-- RF3: Gestão de Inventário (Parte 1: Categorias)
-- Tabela para agrupar produtos (ex: "Enlatados", "Higiene")
CREATE TABLE IF NOT EXISTS categorias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- RF3: Gestão de Inventário (Parte 2: Definição do Produto)
-- Tabela que define o "tipo" de produto
CREATE TABLE IF NOT EXISTS produtos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    categoria_id INT REFERENCES categorias(id) ON DELETE SET NULL, -- Se a categoria for apagada, o produto fica sem categoria
    descricao TEXT
);

-- RF3 & RF6: Gestão de Inventário (Parte 3: O Stock Real)
-- Tabela principal do inventário. Cada linha é um "lote" de um produto
CREATE TABLE IF NOT EXISTS stock_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    produto_id INT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT, -- Não deixa apagar um produto se houver stock dele
    quantidade_inicial INT NOT NULL CHECK (quantidade_inicial > 0),
    quantidade_atual INT NOT NULL CHECK (quantidade_atual >= 0),
    data_entrada DATE NOT NULL DEFAULT CURRENT_DATE,
    data_validade DATE, -- Pode ser NULL para produtos não perecíveis
    campanha_id UUID REFERENCES campanhas(id) ON DELETE SET NULL, -- Proveniência (RF3)
    colaborador_id UUID NOT NULL REFERENCES colaboradores(id) -- Rastreabilidade (RF1)
);

-- RF4: Gestão de Entregas
-- Tabela para agendar e registar as entregas
CREATE TABLE IF NOT EXISTS entregas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    beneficiario_id UUID NOT NULL REFERENCES beneficiarios(id),
    colaborador_id UUID NOT NULL REFERENCES colaboradores(id), -- Quem registou/entregou
    data_agendamento DATE NOT NULL, -- RF4: "sem hora específica"
    estado VARCHAR(50) NOT NULL DEFAULT 'agendada', -- 'agendada', 'entregue', 'nao_entregue'
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- RF4: Gestão de Entregas (Parte 2: Itens da Entrega)
-- Tabela de junção que diz quais "lotes" de stock saíram em cada entrega
CREATE TABLE IF NOT EXISTS detalhes_entrega (
    id SERIAL PRIMARY KEY,
    entrega_id UUID NOT NULL REFERENCES entregas(id) ON DELETE CASCADE, -- Se a entrega for apagada, estes registos também são
    stock_item_id UUID NOT NULL REFERENCES stock_items(id),
    quantidade_entregue INT NOT NULL CHECK (quantidade_entregue > 0)
);

-- RF7: Website Informativo (Formulário de Contacto)
CREATE TABLE IF NOT EXISTS mensagens_contacto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    mensagem TEXT NOT NULL,
    data_rececao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    lida BOOLEAN DEFAULT false
);

-------------------------------------------------------------------
-- PARTE "SUPER PODEROSA": LÓGICA DE NEGÓCIO AUTOMÁTICA (TRIGGERS)
-------------------------------------------------------------------

-- RF3: "Após a confirmação de uma entrega, o stock dos produtos correspondentes deve ser atualizado automaticamente."
-- 1. Criamos a FUNÇÃO que faz a lógica
CREATE OR REPLACE FUNCTION fn_atualizar_stock_apos_entrega()
RETURNS TRIGGER AS $$
DECLARE
    item_detalhe RECORD;
BEGIN
    -- Se o estado da entrega foi mudado PARA "entregue"
    IF NEW.estado = 'entregue' AND OLD.estado != 'entregue' THEN
        -- Itera por todos os itens associados a esta entrega
        FOR item_detalhe IN (SELECT * FROM detalhes_entrega WHERE entrega_id = NEW.id)
        LOOP
            -- Abate a quantidade do lote de stock correspondente
            UPDATE stock_items
            SET quantidade_atual = quantidade_atual - item_detalhe.quantidade_entregue
            WHERE id = item_detalhe.stock_item_id;
        END LOOP;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Criamos o TRIGGER que chama a função
CREATE TRIGGER trg_on_entrega_concluida
AFTER UPDATE ON entregas
FOR EACH ROW
WHEN (NEW.estado = 'entregue' AND OLD.estado IS DISTINCT FROM NEW.estado)
EXECUTE FUNCTION fn_atualizar_stock_apos_entrega();


-------------------------------------------------------------------
-- PARTE "SUPER PODEROSA": VIEW SEGURA PARA O WEBSITE (RF7)
-------------------------------------------------------------------

-- RF7: "Apresentar os produtos... sem exibir as quantidades exatas."
-- Esta VIEW (tabela virtual) pré-calcula os dados para o website
-- A sua API (GET /api/public/stock) só precisa de fazer "SELECT * FROM public_stock_summary"
CREATE OR REPLACE VIEW public_stock_summary AS
SELECT
    cat.nome AS categoria,
    prod.nome AS produto,
    -- Conta quantos "lotes" (registos) existem, mas NÃO a soma das quantidades
    COUNT(DISTINCT si.produto_id) AS disponibilidade,
    -- Mostra a data de validade mais próxima para esse produto
    MIN(si.data_validade) AS validade_proxima
FROM stock_items si
JOIN produtos prod ON si.produto_id = prod.id
JOIN categorias cat ON prod.categoria_id = cat.id
WHERE
    si.quantidade_atual > 0 -- Só mostra produtos que realmente existem em stock
    AND (si.data_validade IS NULL OR si.data_validade > CURRENT_DATE) -- Só mostra produtos válidos
GROUP BY
    cat.nome, prod.nome
ORDER BY
    cat.nome, prod.nome;
