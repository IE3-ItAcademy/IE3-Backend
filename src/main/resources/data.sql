-- =================================================================================
--  DATABASE SEEDING SCRIPT
-- =================================================================================


-- =================================================================================
--  Populate Employees Table
--  Description: Inserts sample employee data.
-- =================================================================================
INSERT INTO Employees (id, name) VALUES
                                     (1, 'Alberto Borsatto'),
                                     (2, 'Fernando Gazzana'),
                                     (3, 'Lucas Murakami'),
                                     (4, 'Matheus Boff'),
                                     (5, 'Eduarda Contudo'),
                                     (6, 'Eduarda Gotuzzo'),
                                     (7,  'Julia Tiethbol'),
                                     (8, 'Ellen Miranda'),
                                     (9, 'Henrique Schultz'),
                                     (10, 'Gustavo Teixeira'),
                                     (11, 'Rafa Vaccaro'),
                                     (12, 'Nicholas Navarro'),
                                     (13, 'Marina Acosta'),
                                     (14, 'Gabriel Velloso');


-- =================================================================================
--  Populate Profiles Table
--  Description: Defines the available roles in the system.
-- =================================================================================
INSERT INTO Profiles (id, role) VALUES
                                    (1, 'MANAGER'),
                                    (2, 'DEV'),
                                    (3, 'QA'),
                                    (4, 'SECURITY');


-- =================================================================================
--  Populate Projects Table
--  Description: Inserts projects with varied timelines relative to the current date.
-- =================================================================================
INSERT INTO Projects (id, name, start_date, end_date, description) VALUES
-- Completed Projects
(1, 'Website Institucional', '2024-06-01 09:00:00-03', '2025-01-31 18:00:00-03', 'Desenvolvimento e lançamento do novo website corporativo.'),
(4, 'Aplicativo de Logística Interna', '2024-11-01 09:00:00-03', '2025-08-15 18:00:00-03', 'Ferramenta mobile para rastreamento e gerenciamento de ativos dentro da empresa.'),
-- Ongoing Projects
(2, 'Sistema de Gestão de Clientes (SGC)', '2025-02-01 09:00:00-03', '2026-08-31 18:00:00-03', 'Desenvolvimento de uma nova plataforma CRM para otimizar o relacionamento com clientes.'),
(3, 'Plataforma E-commerce B2B', '2024-09-15 09:00:00-03', '2025-12-20 18:00:00-03', 'Criação de um portal de vendas online focado no mercado business-to-business.'),
(5, 'Atualização de Segurança de Dados (LGPD)', '2025-05-10 09:00:00-03', '2026-02-20 18:00:00-03', 'Projeto para adequar todos os sistemas da empresa à Lei Geral de Proteção de Dados.'),
-- Future Projects
(6, 'Migração para Nuvem AWS', '2027-01-10 09:00:00-03', '2028-06-30 18:00:00-03', 'Migração completa da infraestrutura on-premise para a nuvem da Amazon Web Services.'),
(7, 'Dashboard de Business Intelligence (BI)', '2026-03-01 09:00:00-03', '2026-09-30 18:00:00-03', 'Criação de painéis de BI para análise de dados de vendas e operações em tempo real.'),
-- No Team Projects
(8, 'Implementação de Sistema ERP', '2022-10-15 09:00:00-03', '2023-05-31 18:00:00-03', 'Desenvolvimento e implantação de sistema ERP para integração dos processos financeiros, logísticos e administrativos.'),
(9, 'Plataforma de E-commerce B2B - 2', '2025-08-15 09:00:00-03', '2026-09-30 18:00:00-03', 'Desenvolvimento de uma plataforma de comércio eletrônico voltada para clientes corporativos, com integração a sistemas de pagamento e logística.'),
(10, 'Automação de Processos com RPA', '2027-07-01 09:00:00-03', '2027-12-15 18:00:00-03', 'Automatização de tarefas repetitivas utilizando Robotic Process Automation para aumentar a eficiência operacional.');


-- =================================================================================
--  Populate Parameters Table
--  Description: Inserts application-level configuration parameters.
-- =================================================================================
INSERT INTO Parameters (id, description, "value") VALUES
    (1, 'Máximo de Horas de trabalho por semana para funcionários', '40');


-- =================================================================================
--  Populate Contracts Table
--  Description: Links employees to contracts, including past and present agreements.
-- =================================================================================
INSERT INTO Contracts (id, start_date, end_date, weekly_hours, wage_by_hour, employee_id) VALUES
-- Active Contracts for current employees
(1, '2024-03-10 09:00:00-03', '2026-03-09 18:00:00-03', 40, 95.50, 1), -- Alberto Borsatto (Manager)
(2, '2024-05-20 09:00:00-03', '2026-05-19 18:00:00-03', 40, 82.00, 2), -- Fernando Gazzana (QA)
(3, '2024-07-01 09:00:00-03', '2025-12-31 18:00:00-03', 35, 68.75, 3), -- Lucas Murakami (DEV)
(4, '2025-01-15 09:00:00-03', '2026-01-14 18:00:00-03', 40, 75.50, 4), -- Matheus Boff (Security)
-- Expired contract for Gabriel Velloso, corresponding to the completed project.
(5, '2024-05-01 09:00:00-03', '2025-02-28 18:00:00-03', 40, 78.25, 14),
-- Gabriel Velloso's current, active contract.
(6, '2025-03-01 09:00:00-03', '2026-02-27 18:00:00-03', 40, 85.00, 14),

(7, '2025-03-01 09:00:00-03', '2026-02-27 18:00:00-03', 40, 15.00, 6),
(8, '2025-03-01 09:00:00-03', '2026-02-27 18:00:00-03', 40, 15.00, 7),
(9, '2025-03-01 09:00:00-03', '2026-02-27 18:00:00-03', 40, 15.00, 8);


-- =================================================================================
--  Populate Employee_Roles (Join Table)
--  Description: Maps which roles are associated with each contract.
-- =================================================================================
INSERT INTO Employee_Roles (contract_Id, profile_Id) VALUES
                                                         (1, 1), -- Alberto's contract -> MANAGER
                                                         (2, 3), -- Fernando's contract -> QA
                                                         (3, 2), -- Lucas's contract -> DEV
                                                         (4, 4), -- Matheus's contract -> SECURITY
                                                         (5, 2), -- Gabriel's old contract -> DEV
                                                         (6, 2), -- Gabriel's new contract -> DEV
                                                         (7, 2),
                                                         (8, 2),
                                                         (9, 2);


-- =================================================================================
--  Populate Alocations Table
--  Description: Allocates employees to projects based on the rule: each non-future
--               project must have exactly 1 Manager, at least 1 DEV, and at least 1 QA.
--               Note: Hours are illustrative to show participation.
-- =================================================================================
INSERT INTO Alocations (id, weekly_hours, employee_role, employee_id, project_id) VALUES
-- Project 1: Website Institucional (Completed)
(1, 10, 'MANAGER', 1, 1),
(2, 40, 'DEV', 14, 1),
(3, 10, 'QA', 2, 1),

-- Project 4: Aplicativo de Logística Interna (Completed)
(4, 10, 'MANAGER', 1, 4),
(5, 35, 'DEV', 3, 4),
(6, 10, 'QA', 2, 4),

-- Project 2: Sistema de Gestão de Clientes (SGC) (Ongoing)
(7, 10, 'MANAGER', 1, 2),
(8, 35, 'DEV', 3, 2),
(9, 40, 'DEV', 14, 2),
(10, 10, 'QA', 2, 2),

-- Project 3: Plataforma E-commerce B2B (Ongoing)
(11, 10, 'MANAGER', 1, 3),
(12, 20, 'DEV', 3, 3),
(13, 20, 'QA', 2, 3),
(14, 20, 'SECURITY', 4, 3),

-- Project 5: Atualização de Segurança de Dados (LGPD) (Ongoing)
(15, 10, 'MANAGER', 1, 5),
(16, 20, 'DEV', 14, 5),
(17, 20, 'QA', 2, 5),
(18, 20, 'SECURITY', 4, 5),

-- Project 6: Migração para Nuvem AWS (Future)
(19, 10, 'MANAGER', 1, 6),
(20, 20, 'DEV', 14, 6),
(21, 20, 'QA', 2, 6),
(22, 20, 'SECURITY', 4, 6),

-- Project 7: Dashboard de Business Intelligence (BI) (Future)
(23, 10, 'MANAGER', 1, 7),
(24, 20, 'DEV', 14, 7),
(25, 20, 'QA', 2, 7),
(26, 20, 'DEV', 6, 7),
(27, 20, 'DEV', 7, 7),
(28, 20, 'DEV', 8, 7),
(29, 20, 'SECURITY', 4, 7);

