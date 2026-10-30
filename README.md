# EBank MS App

Projet pédagogique Spring Boot, Java 21 et Maven, organisé en modules ouvrables directement dans IntelliJ IDEA.

## Architecture

| Module | Port | Rôle |
|---|---:|---|
| `discovery-service` | 8761 | Annuaire Eureka |
| `customer-service` | 8056 | Clients, JPA/H2, REST, Swagger et outils MCP |
| `ebank-service` | 8057 | Comptes, JPA/H2, REST, OpenFeign, Resilience4J et outils MCP |
| `gateway-service` | 8085 | Entrée REST, routes vers les services enregistrés |
| `ebank-bot` | 8060 | Agent Spring AI, client MCP, API REST, Telegram et Discord |

Le compte contient `customerId`. Pour `GET /accounts/{id}`, EBank interroge Customer avec OpenFeign. Un circuit breaker évite qu'une panne du service Customer bloque la lecture du compte : la réponse contient alors `customerStatus: "UNAVAILABLE"`.

## Démarrage dans IntelliJ IDEA

1. Ouvrir le dossier du dépôt et laisser IntelliJ importer les `pom.xml` Maven. Choisir le JDK 21 pour chaque module.
2. Démarrer `DiscoveryServiceApplication` puis ouvrir <http://localhost:8761>.
3. Démarrer `CustomerServiceApplication` et `EbankServiceApplication`, puis `GatewayServiceApplication`.
4. Vérifier Eureka : `CUSTOMER-SERVICE`, `EBANK-SERVICE` et `GATEWAY-SERVICE` doivent être enregistrés.
5. Pour la partie IA, définir une **nouvelle** variable d'environnement `OPENAI_API_KEY` dans la configuration Run d'IntelliJ pour `EbankBotApplication`, puis démarrer ce module après les deux services métier.

Les fichiers `application.properties` contiennent seulement les noms des variables, jamais les secrets. Une clé publiée dans un chat ou un dépôt doit être révoquée et remplacée avant de tester l'agent.

## Partie 1 : API REST

| Opération | URL directe | URL via Gateway |
|---|---|---|
| Clients | `GET/POST http://localhost:8056/customers` | `GET/POST http://localhost:8085/api/customers` |
| Client | `GET http://localhost:8056/customers/{id}` | `GET http://localhost:8085/api/customers/{id}` |
| Comptes | `GET/POST http://localhost:8057/accounts` | `GET/POST http://localhost:8085/api/accounts` |
| Compte | `GET http://localhost:8057/accounts/{id}` | `GET http://localhost:8085/api/accounts/{id}` |

Swagger UI : <http://localhost:8056/swagger-ui.html> et <http://localhost:8057/swagger-ui.html>. Les données H2 sont en mémoire et disparaissent au redémarrage.

Exemple de création de compte dans Postman, avec `Content-Type: application/json` et un `customerId` existant :

```json
{"balance": 1200.0, "type": "CURRENT", "customerId": 1}
```

## Partie 2 : MCP et agent IA

Chaque service métier expose `/mcp` en HTTP Streamable. Customer publie `list_customers` et `get_customer` ; EBank publie `list_accounts` et `get_account`. Les outils sont en lecture seule. Les serveurs et l'agent écoutent sur `127.0.0.1` car les endpoints MCP et les données de cette démonstration ne sont pas authentifiés.

`ebank-bot` se connecte aux deux serveurs comme client MCP. Vérifier la découverte des outils et leur exécution sans appel OpenAI :

- <http://localhost:8060/api/mcp/tools>
- <http://localhost:8060/api/mcp/test>

Tester le chatbot depuis Postman avec `POST http://localhost:8060/api/chat`, `Content-Type: application/json` et :

```json
{"message": "Quels sont les clients disponibles ?"}
```

L'agent utilise `gpt-5-mini` par défaut. Un compte OpenAI avec accès API et crédits est nécessaire pour cet appel.

### Telegram

Créer un bot avec BotFather, puis définir dans la configuration Run d'IntelliJ : `TELEGRAM_BOT_ENABLED=true`, `TELEGRAM_BOT_TOKEN` et `TELEGRAM_ALLOWED_CHAT_ID`. L'identifiant de chat autorisé évite d'exposer les données à tous les utilisateurs du bot. Démarrer `EbankBotApplication`, puis envoyer un message au bot. Il utilise le même `ChatService` et les mêmes outils MCP que l'API REST.

### Discord

Créer une application et son bot dans le portail développeur Discord, l'inviter avec la permission `applications.commands`, puis définir `DISCORD_BOT_ENABLED=true`, `DISCORD_BOT_TOKEN` et `DISCORD_ALLOWED_USER_ID` dans la configuration Run d'IntelliJ. Démarrer `EbankBotApplication` et utiliser `/ebank question:...`. Seul l'utilisateur indiqué peut interroger le bot.

## Vérification Maven

Dans le terminal IntelliJ, depuis la racine du dépôt, lancer pour chaque module :

```powershell
.\customer-service\mvnw.cmd -f .\customer-service\pom.xml -DskipTests package
.\customer-service\mvnw.cmd -f .\ebank-service\pom.xml -DskipTests package
.\customer-service\mvnw.cmd -f .\discovery-service\pom.xml -DskipTests package
.\customer-service\mvnw.cmd -f .\gateway-service\pom.xml -DskipTests package
.\customer-service\mvnw.cmd -f .\ebank-bot\pom.xml -DskipTests package
```
