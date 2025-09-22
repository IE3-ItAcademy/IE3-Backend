-- =================================================================================
--  DATABASE SEEDING SCRIPT
--  Current Date Context: September 22, 2025
--  Description: This script populates the database with a logical set of data,
--               including past, present, and future records to simulate a real-world environment.
-- =================================================================================


-- =================================================================================
--  Populate Employees Table
--  Description: Inserts sample employee data.
-- =================================================================================
INSERT INTO Employees (id, name) VALUES
                                     (1, 'Alberto Borsatto'),
                                     (2, 'Fernando Gazzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaanaeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee'),
                                     (3, 'Lucas Murakami'),
                                     (4, 'Matheus Boff'),
                                     (5, 'Gabriel Velloso');


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
(6, 'Migração para Nuvem AWS', '2026-01-10 09:00:00-03', '2026-06-30 18:00:00-03', 'Migração completa da infraestrutura on-premise para a nuvem da Amazon Web Services.'),
(7, 'Dashboard de Business Intelligence (BI)', '2026-03-01 09:00:00-03', '2026-09-30 18:00:00-03', 'Criação de painéis de BI para análise de dados de vendas e operações em tempo real.');


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
INSERT INTO Contracts (id, start_date, end_date, weekly_hours, wage_by_hour, users_id) VALUES
-- Active Contracts for current employees
(1, '2024-03-10 09:00:00-03', '2026-03-09 18:00:00-03', 40, 95.50, 1), -- Alberto Borsatto (Manager)
(2, '2024-05-20 09:00:00-03', '2026-05-19 18:00:00-03', 40, 82.00, 2), -- Fernando Gazzana (QA)
(3, '2024-07-01 09:00:00-03', '2025-12-31 18:00:00-03', 35, 68.75, 3), -- Lucas Murakami (DEV)
(4, '2025-01-15 09:00:00-03', '2026-01-14 18:00:00-03', 40, 75.50, 4), -- Matheus Boff (Security)
-- Expired contract for Gabriel Velloso, corresponding to the completed project.
(5, '2024-05-01 09:00:00-03', '2025-02-28 18:00:00-03', 40, 78.25, 5),
-- Gabriel Velloso's current, active contract.
(6, '2025-03-01 09:00:00-03', '2026-02-27 18:00:00-03', 40, 85.00, 5);


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
                                                         (6, 2); -- Gabriel's new contract -> DEV


-- =================================================================================
--  Populate Alocations Table
--  Description: Allocates employees to specific projects, ensuring consistency with project and contract timelines.
-- =================================================================================
INSERT INTO Alocations (id, weekly_hours, employee_role, user_id, project_id) VALUES
-- Allocation for the completed project.
(1, 40, 'DEV', 5, 1),    -- Gabriel Velloso on Website Institucional
-- Allocations for the ongoing SGC project.
(2, 20, 'MANAGER', 1, 2), -- Alberto Borsatto (Manager)
(3, 35, 'DEV', 3, 2),    -- Lucas Murakami (Dev) on SGC Project
(4, 40, 'DEV', 5, 2),    -- Gabriel Velloso (Dev)
-- Allocations for the ongoing E-commerce project.
(5, 40, 'QA', 2, 3),       -- Fernando Gazzana (QA) on E-commerce Project
(6, 40, 'SECURITY', 4, 3); -- Matheus Boff (Security)

