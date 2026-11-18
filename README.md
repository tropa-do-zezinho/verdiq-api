# Verdiq API

Backend Spring Boot do **Verdiq** (GS-4S): calendário de safra FLV, ERP simples do comerciante, motor sazonal, ofertas, planos sem pagamento e ETL de fontes públicas.

Banco: **Oracle** (SID). Documentação acadêmica: [`../docs/Verdiq-Documentacao.md`](../docs/Verdiq-Documentacao.md).

## Stack

- Java 25 / Spring Boot 4.1
- Oracle Database + Flyway
- JWT (access + refresh)
- OpenAPI/Swagger (`/swagger-ui.html`)
- Conectores: PROHORT/Conab, BCB (IPCA), SIDRA/IBGE, probe Ceagesp

Fora desta entrega: envio por e-mail e WhatsApp.

## Variáveis de ambiente (Oracle)

Copie [`.env.example`](.env.example) e exporte (ou configure no IntelliJ → Run → Environment):

| Variável          | Descrição                    |
| ----------------- | ---------------------------- |
| `ORACLE_USERNAME` | Usuário do schema            |
| `ORACLE_PASSWORD` | Senha                        |
| `ORACLE_HOSTNAME` | Host (ex.: IP do banco prod) |
| `ORACLE_PORT`     | Porta (default `1521`)       |
| `ORACLE_SID`      | SID da instância             |

JDBC montado: `jdbc:oracle:thin:@HOST:PORT:SID`

## Docker (deploy)

```bash
cd back
docker build -t verdiq-api .
docker run --rm -p 8080:8080 \
  -e ORACLE_USERNAME=... \
  -e ORACLE_PASSWORD=... \
  -e ORACLE_HOSTNAME=... \
  -e ORACLE_PORT=1521 \
  -e ORACLE_SID=... \
  -e PORT=8080 \
  verdiq-api
```

No Render/Railway: builder **Dockerfile**, root `back`, sem start command custom (usa o `ENTRYPOINT` da imagem). Variáveis `ORACLE_*` + `PORT`.

Flyway usa `baseline-on-migrate=true` e `baseline-version=0` para schemas FIAP já não-vazios sem tabela de histórico.

## Subir local

```bash
cd back
export ORACLE_USERNAME=... ORACLE_PASSWORD=... ORACLE_HOSTNAME=... ORACLE_PORT=1521 ORACLE_SID=...
./mvnw spring-boot:run
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`

Oracle XE opcional via `docker compose up -d` (imagem `gvenzl/oracle-xe`); em produção use o banco institucional com as variáveis acima. Compose **não** sobe automaticamente (`spring.docker.compose.enabled=false`).

### Usuários seed (senha `Senha@123`)

| E-mail                | Papel                   |
| --------------------- | ----------------------- |
| `admin@naepoca.local` | ADMIN                   |
| `marta@naepoca.local` | COMERCIANTE (plano PRO) |
| `ana@naepoca.local`   | CLIENTE                 |

## Postman

1. Importar [`postman/NaEpoca_API.postman_collection.json`](postman/NaEpoca_API.postman_collection.json)
2. Importar [`postman/NaEpoca_Local.postman_environment.json`](postman/NaEpoca_Local.postman_environment.json)

## ETL manual

```http
POST /v1/admin/etl/run
```

Upload de preços (CSV): `POST /v1/admin/market-uploads`

## Módulos

`auth` · `catalog` · `market` · `seasonality` · `merchant` · `offers` · `notifications` · `plans` · `admin` · `etl`
