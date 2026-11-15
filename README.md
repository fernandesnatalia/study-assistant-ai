# Study Assistant AI

Study Assistant AI é uma aplicação backend para organização e apoio aos estudos, desenvolvida em **Java 21 e Spring Boot**.

A plataforma permite estruturar materiais de estudo, criar anotações, organizar flashcards, praticar respostas e acompanhar desempenho. Recursos de **Inteligência Artificial** são integrados ao backend para geração de conteúdos e flashcards, avaliação de respostas e assistência ao estudo.

A arquitetura mantém as regras de negócio, autorização, isolamento de dados e persistência sob responsabilidade da aplicação. A IA atua somente nas operações em que sua utilização é explicitamente solicitada.

---

## Funcionalidades

### Organização dos estudos

- Cadastro e autenticação de usuários.
- Organização de estudos em **matérias, tópicos e conteúdos**.
- Relacionamento hierárquico `Subject → Topic → Content`.
- Criação de anotações associadas a matérias, tópicos ou conteúdos.
- Isolamento dos dados entre usuários.
- Operações de criação, consulta, atualização e exclusão dos recursos.

### Baralhos e flashcards

- Criação de baralhos temáticos.
- Criação manual de flashcards.
- Geração de respostas com IA.
- Geração completa de perguntas e respostas com IA.
- Geração de múltiplos flashcards em uma solicitação.
- Utilização opcional de conteúdo, tópico ou contexto textual para orientar a geração.
- Adição posterior de novos flashcards a baralhos existentes.
- Isolamento dos baralhos e flashcards por usuário.

O tema do baralho determina seu escopo conceitual e permanece independente da estrutura de matérias, tópicos e conteúdos.

### Treino

Baralhos podem ser utilizados para treino com flashcards gerados para esse fluxo.

Durante o treino:

1. o usuário recebe uma pergunta sem visualizar previamente a resposta esperada;
2. fornece uma resposta textual ou desiste da tentativa;
3. a IA compara a resposta fornecida com a resposta esperada;
4. o resultado é classificado como `CORRECT` ou `INCORRECT`;
5. a tentativa é registrada individualmente como `AnswerAttempt`.

Tentativas anteriores são preservadas enquanto o baralho permanecer utilizado para treino.

### Desempenho e revisões

O desempenho é calculado a partir dos registros de `AnswerAttempt`, sem persistência duplicada das métricas derivadas.

São calculados:

- total de tentativas;
- respostas corretas;
- respostas incorretas;
- percentual de acertos.

A necessidade de revisão também é derivada do histórico.

Um flashcard é considerado pendente quando sua tentativa mais recente possui resultado `INCORRECT`. Uma tentativa posterior com resultado `CORRECT` remove essa pendência.

### Assistente de estudos

O assistente utiliza IA para:

- responder perguntas;
- esclarecer dúvidas;
- explicar conceitos;
- aprofundar assuntos;
- apresentar informações de outras formas.

As interações são independentes dos demais recursos da aplicação e não criam ou modificam automaticamente matérias, tópicos, conteúdos, anotações, baralhos, flashcards ou tentativas.

### Geração de conteúdos com IA

A aplicação permite gerar materiais de estudo a partir de:

- um tópico pertencente ao usuário;
- uma orientação textual opcional.

A IA retorna título e conteúdo textual.

O resultado permanece temporário até a aprovação explícita do usuário. Somente após essa aprovação o conteúdo é persistido e passa a se comportar como qualquer outro conteúdo da aplicação.

---

## Arquitetura

A aplicação utiliza uma arquitetura em camadas, separando a exposição da API REST, as regras de negócio, a persistência e a integração com modelos de Inteligência Artificial.

```text
Client
  │
  ▼
REST API
  │
  ▼
Controllers
  │
  ▼
Services ──────────────► AI Services
  │                         │
  │                         ▼
  │                  GenerativeService
  │                         │
  │                         ▼
  │                  Spring AI / Ollama
  │                         │
  │                         ▼
  │                        LLM
  ▼
Repositories
  │
  ▼
PostgreSQL
```

### Responsabilidades

**Controllers**

Expõem os endpoints REST, recebem as requisições e encaminham as operações para os serviços responsáveis.

**Services**

Concentram as regras de negócio, validações, autorização e coordenação das operações da aplicação.

**Repositories**

Abstraem o acesso aos dados persistidos no PostgreSQL por meio do Spring Data JPA e fornecem consultas utilizadas na aplicação das regras de isolamento multiusuário.

**AI Services**

Coordenam as funcionalidades de IA de acordo com suas responsabilidades:

- `ContentAiService`: geração de conteúdos de estudo.
- `FlashcardAiService`: geração de perguntas, respostas e flashcards.
- `AnswerEvaluationAiService`: avaliação das respostas fornecidas durante o treino.
- `StudyAssistantService`: interações com o assistente de estudos.

**GenerativeService**

Centraliza a comunicação com o modelo de linguagem utilizando Spring AI e Ollama, evitando que os serviços responsáveis pelas funcionalidades assumam diretamente essa integração.

### Separação entre backend e IA

O modelo de linguagem não possui acesso direto ao banco de dados.

O backend identifica o usuário autenticado, valida a operação, seleciona o contexto autorizado e controla o tratamento dos resultados produzidos pela IA.

As regras de negócio, autorização e persistência permanecem sob responsabilidade da aplicação, independentemente do modelo de linguagem utilizado.

---

## Modelo de domínio

A organização principal dos estudos segue a estrutura:

```text
User
 │
 ├── Subject
 │    │
 │    └── Topic
 │         │
 │         └── Content
 │
 ├── Note
 │
 └── Deck
      │
      └── Flashcard
           │
           └── AnswerAttempt
```

### Organização dos conteúdos

```text
Subject 1 ─── N Topic
Topic   1 ─── N Content
```

Todo tópico pertence obrigatoriamente a uma matéria e todo conteúdo pertence obrigatoriamente a um tópico.

### Anotações

Cada `Note` pertence ao usuário e deve possuir exatamente um contexto:

```text
Subject
   OU
Topic
   OU
Content
```

Um usuário pode possuir no máximo uma anotação para cada contexto.

Quando o recurso associado é excluído, sua anotação também é removida, inclusive quando a exclusão ocorre em cascata.

### Baralhos

`Deck` e `Flashcard` são estruturalmente independentes de `Subject`, `Topic` e `Content`.

Essa separação permite criar um baralho para qualquer tema sem exigir que exista previamente uma estrutura equivalente na organização dos estudos.

Conteúdos e tópicos podem, entretanto, ser utilizados como contexto explícito para determinadas operações de geração com IA.

---

## Integração com Inteligência Artificial

A IA é tratada como uma capacidade integrada à aplicação, enquanto o backend permanece responsável pelo domínio.

```text
Usuário
   │
   ▼
Endpoint
   │
   ▼
Validação da operação
   │
   ▼
Resolução e validação do contexto
   │
   ▼
Serviço de IA
   │
   ▼
Modelo de linguagem
   │
   ▼
Validação do resultado
   │
   ▼
Regra de negócio / Persistência
```

### Operações disponíveis

A primeira versão utiliza IA para quatro responsabilidades principais:

| Operação | Responsabilidade |
|---|---|
| Assistente de estudos | Responder perguntas e auxiliar na compreensão de conceitos |
| Geração de conteúdo | Produzir materiais de estudo sujeitos à aprovação do usuário |
| Geração de flashcards | Produzir perguntas e respostas dentro do escopo solicitado |
| Avaliação de respostas | Comparar a resposta do usuário com a resposta esperada durante o treino |

As operações são independentes e executadas somente quando solicitadas pelo usuário.

Uma operação de IA não inicia automaticamente outra operação, exceto quando esse encadeamento fizer parte explicitamente do fluxo solicitado.

---

## Segurança e isolamento de dados

A API utiliza **Spring Security e JWT** para autenticação e autorização.

A identidade utilizada para controle de acesso é obtida pelo backend a partir da credencial autenticada.

```text
Request
   │
   ▼
  JWT
   │
   ▼
Usuário autenticado
   │
   ▼
Validação de propriedade
   │
   ▼
Recurso
```

O cliente não determina a identidade utilizada para autorização por meio de um `userId`.

As regras de propriedade são aplicadas aos recursos da aplicação para impedir acesso cruzado entre usuários.

Isso inclui:

- matérias;
- tópicos;
- conteúdos;
- anotações;
- baralhos;
- flashcards;
- tentativas de resposta;
- contextos fornecidos às operações de IA.

---

## Regras de domínio relevantes

Algumas decisões do domínio possuem impacto direto sobre o comportamento da aplicação.

### Contexto dos flashcards

O tema do baralho determina o escopo conceitual utilizado na geração.

Conteúdos, tópicos ou contexto textual podem delimitar uma geração quando forem explicitamente fornecidos para essa finalidade.

Informações inseridas manualmente em outros flashcards não são utilizadas automaticamente como contexto para novas gerações.

### Baralhos de treino

Enquanto um baralho estiver sendo utilizado para treino:

- somente flashcards compatíveis com esse fluxo podem ser adicionados;
- seus flashcards não podem ser alterados manualmente;
- as tentativas são preservadas para cálculo de desempenho e revisão.

Quando o baralho deixa de ser utilizado para treino, suas tentativas são removidas e as métricas derivadas deixam de estar disponíveis.

### Exclusão do último flashcard

Na primeira versão, um baralho não permanece persistido sem flashcards.

A exclusão do último flashcard provoca também a exclusão do baralho.

O tratamento de um estado vazio transitório ficará sob responsabilidade da aplicação Web em uma versão posterior.

### Resultados gerados por IA

A geração não implica persistência automática.

Quando a funcionalidade exige validação ou aprovação, o resultado produzido pela IA permanece separado dos dados persistidos até a confirmação do usuário.

---

## Tecnologias

### Backend - V1

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot | Estrutura da aplicação |
| Spring MVC | API REST |
| Spring Data JPA | Persistência e acesso aos dados |
| Hibernate | ORM |
| PostgreSQL | Banco de dados relacional |
| Spring Security | Segurança da aplicação |
| JWT | Autenticação e identificação do usuário |
| Spring AI | Integração com modelos de IA |
| Ollama | Execução local dos modelos |
| Qwen 2.5 1.5B | Modelo de linguagem inicial |
| Docker | Execução da infraestrutura |
| Maven | Build e gerenciamento de dependências |
| JUnit 5 | Testes automatizados |
| Mockito | Testes unitários e isolamento de dependências |

### Tecnologias previstas para as próximas versões

| Tecnologia | Utilização prevista |
|---|---|
| Apache Tika | Extração de texto de documentos |
| Spring AI + Ollama | Geração local de embeddings |
| PostgreSQL + pgvector | Armazenamento e busca vetorial |
| RAG | Recuperação de contexto para operações de IA |
| React | Aplicação Web |

---

## Estratégia de testes

A primeira versão inclui testes automatizados das regras implementadas, abrangendo serviços, validações, isolamento entre usuários e operações integradas à IA.

Além dos testes automatizados, os fluxos completos da API são validados por meio do arquivo:

```text
requests.http
```

Os cenários de integração cobrem:

- cadastro e autenticação;
- autorização e isolamento multiusuário;
- organização de matérias, tópicos e conteúdos;
- anotações e regras de associação;
- baralhos e flashcards;
- restrições de treino;
- tentativas de resposta;
- geração de conteúdos com IA;
- geração de flashcards com IA;
- avaliação de respostas;
- assistente de estudos;
- cálculo de desempenho;
- identificação de revisões;
- validações e cenários de erro;
- exclusões e operações em cascata.

O arquivo funciona também como documentação executável dos principais fluxos da API.

---

## Ambiente de desenvolvimento e execução

### Pré-requisitos

Para executar a aplicação localmente, são necessários:

- JDK 21.
- Docker com Docker Compose.
- Ollama instalado e em execução.
- Modelo `qwen2.5:1.5b` disponível no Ollama.
- Configuração local de conexão com o PostgreSQL e JWT.

### Modelo de linguagem

O projeto utiliza inicialmente o modelo Qwen 2.5 1.5B, executado localmente pelo Ollama.

Para disponibilizar o modelo:

```bash
ollama pull qwen2.5:1.5b
```

A integração utiliza Spring AI, com as seguintes propriedades:

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen2.5:1.5b
spring.ai.ollama.chat.options.temperature=0.2
```

### Banco de dados

O PostgreSQL 17 é executado em container Docker.

Para iniciar a infraestrutura:

```bash
docker compose up -d
```

A persistência utiliza Spring Data JPA e Hibernate.

No ambiente de desenvolvimento, o esquema do banco de dados é atualizado por meio da configuração:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Essa configuração é destinada ao desenvolvimento.

### Executar a aplicação

Com o PostgreSQL e o Ollama em execução e as configurações locais definidas, iniciar o backend:

```cmd
mvnw.cmd spring-boot:run
```

A API estará disponível em:

```text
http://localhost:8080
```

### Executar os testes

Para executar a suíte de testes automatizados:

```cmd
mvnw.cmd test
```

Os testes utilizam JUnit 5 e Mockito. Os fluxos HTTP permitem validar adicionalmente a integração entre os componentes da aplicação e o modelo de linguagem.

---

## Exemplos de utilização da API

O repositório inclui o arquivo `requests.http`, utilizado para executar e validar os principais fluxos da aplicação por meio do HTTP Client do IntelliJ IDEA.

As requisições estão organizadas em uma sequência que demonstra operações CRUD, relacionamentos entre entidades, autenticação, isolamento multiusuário, funcionalidades de IA, regras de domínio e cenários de erro.

### Cadastro de usuário

```http
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "name": "Augusta Ada Byron King",
  "email": "ada@example.com",
  "password": "ada123"
}
```

### Autenticação

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "ada@example.com",
  "password": "ada123"
}
```

A autenticação retorna um token JWT, utilizado nas requisições protegidas.

### Criação de matéria

```http
POST http://localhost:8080/api/subjects
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "Matemática"
}
```

### Criação de tópico

```http
POST http://localhost:8080/api/topics?subjectId=1
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "Algoritmos"
}
```

O tópico é associado à matéria informada, desde que ela pertença ao usuário autenticado.

Os demais fluxos, incluindo geração de conteúdos e flashcards com IA, treino, desempenho, revisões e validações de segurança, estão documentados no arquivo `requests.http`.

Cada requisição possui a indicação do status HTTP esperado, permitindo acompanhar o comportamento da API durante os testes.

---

## Evolução do projeto

O desenvolvimento é organizado em versões funcionais para permitir a evolução independente do backend e da interface.

### V1 - Backend e funcionalidades principais

A primeira versão concentra a implementação da API REST e do domínio principal da aplicação.

Inclui:

- organização de estudos;
- matérias, tópicos e conteúdos;
- anotações;
- baralhos e flashcards;
- treino e tentativas de resposta;
- autenticação e isolamento multiusuário;
- integração com IA;
- geração de conteúdos;
- geração de flashcards;
- avaliação de respostas;
- assistente de estudos;
- desempenho;
- revisões;
- validação integrada da API.

### V2 - Documentos e RAG

A segunda versão ampliará as fontes de conhecimento utilizadas pela plataforma.

Está prevista a implementação de:

- upload de documentos;
- extração textual com Apache Tika;
- transformação do material em conteúdos de estudo;
- preservação da origem documental;
- geração de embeddings;
- armazenamento vetorial com `pgvector`;
- recuperação semântica de informações;
- RAG;
- rastreabilidade das fontes;
- integração do contexto recuperado às operações de IA.

A recuperação será restrita aos conteúdos pertencentes ao usuário autenticado.

O RAG será uma evolução da estratégia de contexto das operações existentes, e não uma substituição das funcionalidades de IA.

### V3 - Aplicação Web

A terceira versão adicionará uma aplicação Web em **React**, consumindo a API REST existente.

A interface deverá disponibilizar os fluxos de:

- autenticação;
- organização dos estudos;
- conteúdos e anotações;
- funcionalidades de IA;
- baralhos e flashcards;
- treino;
- desempenho e revisões;
- documentos e fontes utilizadas pelo RAG.

A API permanecerá como contrato de comunicação entre frontend e backend.

Uma proposta inicial para o dashboard da aplicação Web é apresentada abaixo:

![Mockup do dashboard da aplicação Web](src/docs/dashboard-mockup.png)

---

## Princípios técnicos

As principais decisões técnicas da aplicação são:

- **Backend como responsável pelo domínio:** regras de negócio, validações, autorização e persistência permanecem sob controle da aplicação, sem delegação dessas responsabilidades ao modelo de IA.
- **IA integrada e sob demanda:** funcionalidades generativas são executadas mediante solicitação do usuário, com comunicação centralizada e separada das regras de negócio, permitindo a substituição do modelo de linguagem sem reestruturar o domínio.
- **Segurança e isolamento multiusuário:** a identidade autenticada determina o acesso aos recursos e aos contextos utilizados nas operações de IA, impedindo acesso cruzado entre usuários.
- **Consistência e integridade dos dados:** resultados que exigem aprovação são validados antes da persistência, enquanto métricas de desempenho e revisões são derivadas das tentativas registradas, evitando armazenamento redundante.
- **Evolução incremental:** a arquitetura preserva a separação entre backend, integração com IA e interface, permitindo incorporar documentos, RAG e aplicação Web nas próximas versões.

---

## Status

**V1 - Backend e funcionalidades principais: concluída.**

A API contempla o domínio principal da plataforma, autenticação e isolamento multiusuário, integração inicial com IA, fluxos de treino, desempenho e revisão, além da validação integrada das regras de negócio.

**Próxima versão:** V2 - Documentos e RAG.