# Pessoa 2 - Solicitacao de Licenca/Folga

Este arquivo explica a contribuicao individual da jornada **Pessoa 2 - Solicitacao de Licenca/Folga** no Trabalho T2 de Verificacao e Validacao de Software sobre o OrangeHRM.

## Jornada testada

A jornada definida para a Pessoa 2 foi:

1. Funcionario faz login no OrangeHRM.
2. Funcionario solicita um periodo de licenca/folga.
3. Sistema registra a solicitacao como pendente.
4. Administrador faz login.
5. Administrador aprova ou rejeita a solicitacao.
6. Sistema remove a solicitacao da lista de pendentes e atualiza o estado.

Depois da revisao do enunciado, a jornada passou a considerar diretamente o OrangeHRM real como sistema sob teste. A copia local do fonte oficial foi usada para analise, e o E2E foi implementado contra o servidor demo real.

## Fonte oficial analisado

O OrangeHRM oficial foi clonado localmente em `external/orangehrm/`, com a pasta ignorada pelo Git do trabalho:

```bash
git clone --depth 1 https://github.com/orangehrm/orangehrm.git external/orangehrm
rm -rf external/orangehrm/.git
```

Versao analisada:

```text
56e23b3b09e7af29317a0943523b825843fff527
OHRM5X-2669: Update Dockerfile for OrangeHRM 5.9 (#1962)
```

Modulo real de licencas:

```text
external/orangehrm/src/plugins/orangehrmLeavePlugin
```

Arquivos relevantes do fonte oficial:

- `Api/MyLeaveRequestAPI.php`: API para o funcionario criar, listar e consultar suas solicitacoes.
- `Api/EmployeeLeaveRequestAPI.php`: API usada por administrador/supervisor para consultar e atualizar solicitacoes de funcionarios.
- `Service/LeaveRequestService.php`: servico de dominio para requisicoes de licenca.
- `Dao/LeaveRequestDao.php`: acesso a persistencia das solicitacoes.
- `entity/LeaveRequest.php` e `entity/Leave.php`: entidades de solicitacao e dias de licenca.
- `test/Api/MyLeaveRequestAPITest.php`: testes existentes da API de solicitacao do proprio funcionario.
- `test/Api/EmployeeLeaveRequestAPITest.php`: testes existentes da API de administracao/atualizacao de solicitacoes.
- `test/Service/LeaveRequestServiceTest.php`: testes existentes do servico de solicitacao.
- `test/Dao/LeaveRequestDaoTest.php`: testes existentes de persistencia/consulta.
- `test/Api/LeaveOverlapAPITest.php`: testes existentes de sobreposicao de periodos.

Essa analise mostra que o OrangeHRM oficial ja possui boa cobertura em nivel de API, servico, DAO e fixtures. O que faltava no nosso repositorio era um teste de sistema/E2E da jornada completa no navegador.

## Arquivos criados ou alterados

Documentacao:

- `relatorio_pessoa2_licenca.tex`
- `PESSOA2_LICENCA_TESTES.md`

Configuracao E2E:

- `.gitignore`
- `package.json`
- `package-lock.json`
- `playwright.config.js`
- `tests/e2e/orangehrm-leave-request.spec.js`

Testes Java auxiliares:

- `tf-verival/src/main/java/br/pucrs/verival/LeaveRequest.java`
- `tf-verival/src/main/java/br/pucrs/verival/LeaveRequestService.java`
- `tf-verival/src/main/java/br/pucrs/verival/LeaveRequestStatus.java`
- `tf-verival/src/main/java/br/pucrs/verival/LeaveType.java`
- `tf-verival/src/test/java/br/pucrs/verival/LeaveRequestTest.java`
- `tf-verival/src/test/java/br/pucrs/verival/LeaveRequestServiceIntegrationTest.java`
- `tf-verival/src/test/java/br/pucrs/verival/LeaveRequestJourneySystemTest.java`
- `tf-verival/src/test/java/br/pucrs/verival/OrangeHrmLeaveE2EPreflightTest.java`

Observacao importante: os testes Java sao uma **modelagem auxiliar** para demonstrar tecnicas unitarias e de integracao no repositorio da turma. O teste que exercita o OrangeHRM real e o Playwright em `tests/e2e/orangehrm-leave-request.spec.js`.

## Metodos e tecnicas aplicadas

- **Analise estatica/exploratoria do fonte oficial:** localizacao do plugin real `orangehrmLeavePlugin` e dos testes existentes.
- **Particionamento de equivalencia:** dados validos e invalidos para solicitacao local auxiliar.
- **Analise de valor limite:** periodo de um dia e periodo com data final anterior a inicial.
- **Teste de transicao de estados:** pendente para aprovado e pendente para rejeitado.
- **Teste negativo:** decisao sem administrador, solicitacao inexistente e periodo sobreposto.
- **Teste de integracao:** servico local auxiliar integrando criacao, consulta e decisao.
- **Teste de sistema/E2E:** Playwright exercitando navegador, login, cadastro de funcionario, entitlement, solicitacao e decisao administrativa no OrangeHRM demo.

## Casos de teste

| Caso | Descricao | Nivel | Implementacao |
| --- | --- | --- | --- |
| CT-LIC-01 | Solicitacao valida inicia pendente | Unitario | `LeaveRequestTest` |
| CT-LIC-02 | Funcionario vazio lanca excecao | Unitario | `LeaveRequestTest` |
| CT-LIC-03 | Tipo de licenca nulo lanca excecao | Unitario | `LeaveRequestTest` |
| CT-LIC-04 | Datas obrigatorias lancam excecao | Unitario | `LeaveRequestTest` |
| CT-LIC-05 | Data final anterior a inicial lanca excecao | Unitario | `LeaveRequestTest` |
| CT-LIC-06 | Periodo de um dia e aceito | Unitario | `LeaveRequestTest` |
| CT-LIC-07 | Admin aprova solicitacao pendente | Integracao/Sistema | `LeaveRequestServiceIntegrationTest` e Playwright |
| CT-LIC-08 | Admin rejeita solicitacao pendente | Integracao/Sistema | `LeaveRequestServiceIntegrationTest` e Playwright |
| CT-LIC-09 | Periodo sobreposto ativo e bloqueado | Integracao | `LeaveRequestServiceIntegrationTest` |
| CT-LIC-10 | Periodo rejeitado permite nova solicitacao | Integracao | `LeaveRequestServiceIntegrationTest` |
| CT-LIC-11 | Solicitacao inexistente lanca excecao | Integracao | `LeaveRequestServiceIntegrationTest` |
| CT-LIC-12 | Funcionario solicita e admin aprova no OrangeHRM demo | E2E real | `orangehrm-leave-request.spec.js` |
| CT-LIC-13 | Funcionario solicita e admin rejeita no OrangeHRM demo | E2E real | `orangehrm-leave-request.spec.js` |

## Como o E2E real funciona

O teste Playwright executa os seguintes passos no servidor demo:

1. Login como administrador usando `Admin/admin123`, ou variaveis `ORANGEHRM_ADMIN_USERNAME` e `ORANGEHRM_ADMIN_PASSWORD`.
2. Criacao de um funcionario temporario com usuario de login.
3. Inclusao de entitlement de licenca para esse funcionario.
4. Logout do administrador.
5. Login como funcionario criado.
6. Acesso a `Leave > Apply`.
7. Solicitacao de licenca com tipo `CAN - Vacation` e periodo `2026-22-12` a `2026-23-12`.
8. Logout do funcionario.
9. Login como administrador.
10. Busca da solicitacao pendente em `Leave List`.
11. Aprovacao no primeiro cenario.
12. Rejeicao no segundo cenario.
13. Verificacao de que a solicitacao saiu da lista de pendentes apos a decisao.

O formato de data usado pelo demo e `yyyy-dd-mm`, por isso as datas aparecem como `2026-22-12` e `2026-23-12`.

## Como executar

Instalar dependencias Node:

```bash
npm install
```

Executar E2E real:

```bash
ORANGEHRM_E2E_ENABLED=true npx playwright test
```

Executar testes Java locais:

```bash
cd tf-verival
mvn test
```

## Resultados obtidos

Baseline antes das alteracoes:

```text
mvn test
12 testes executados, 0 falhas, 0 erros, 0 ignorados.
```

Execucao Java apos a implementacao local auxiliar:

```text
mvn test
35 testes executados, 0 falhas, 0 erros, 1 ignorado.
```

O teste ignorado e `OrangeHrmLeaveE2EPreflightTest`, porque o E2E principal agora esta em Playwright.

Execucao Playwright contra o OrangeHRM demo:

```text
ORANGEHRM_E2E_ENABLED=true npx playwright test
2 testes executados, 2 passaram.
Tempo: aproximadamente 1.6 min.
```

Cenarios E2E que passaram:

- funcionario solicita licenca e admin aprova a solicitacao;
- funcionario solicita licenca e admin rejeita a solicitacao.

## Limitacoes

- O servidor demo e compartilhado e pode ser alterado por outras pessoas.
- O teste E2E cria funcionarios temporarios e altera dados do demo.
- As credenciais padrao do demo podem mudar.
- Os testes PHP oficiais do OrangeHRM foram analisados no clone local, mas nao foram executados dentro deste repositorio porque exigem o ambiente completo do OrangeHRM.
- A modelagem Java local nao substitui o fonte oficial; ela serve como artefato auxiliar para demonstrar tecnicas de teste unitario e integracao.

## O que revisar antes da entrega

Preencher ou revisar:

```text
Aluno: [TEU NOME]
Usuario GitHub: [TEU USUARIO]
Issue: #[NUMERO]
Video individual: [LINK]
Data de execucao: [DATA]
```

Tambem revisar se a turma quer que este conteudo seja migrado para Wiki.

## Registro de uso de IA

Este material foi elaborado com auxilio da ferramenta ChatGPT/Codex e deve ser revisado pelo aluno antes da entrega. O aluno e responsavel por validar o conteudo, registrar Issue, fazer commits e adequar o texto as orientacoes da disciplina.
