# Implementação de uma API em Java com Hibernate e Spring

#### [Link do Repositório](https://github.com/gabriel-a-muller/api_rest_test)

Olá, esse documento consiste na explicação da implementação da API *FIPE HTTP REST* passo-a-passo.

Irá ser apresentado as tecnologias utilizadas, a justificativa de cada uma e possíveis códigos exemplos que possam esclarecer o uso das ferramentas na prática.

O que você precisa saber ao ler este documento:

- Java (Para compreensão de classes e tipos de variável)

O que você aprenderá ao ler este documento:

- Utilizar o Hibernate para aquisição e persistência de modelos com o banco de dados.
- Criação de Controladores Rest com Spring-Framework.
- Utilizar GET e POST com os controladores.
- Enviar e receber dados json com Jackson. 
- Personalizar mensagem de status e o próprio código de status da resposta HTML.

O que você NÃO aprenderá ao ler este documento:

- Como criar um banco de dados e a tabela para cada modelo.
- Como instalar cada ferramenta.

Tecnologias Utilizadas:

- [hibernate-core](#implementação-da-classes-modelos-user-e-vehicle-com-hibernate)
- [mysql-connector-java](#implementação-da-classes-modelos-user-e-vehicle-com-hibernate)
- [jackson-databind](#jackson)
- [spring-webmvc](#estrutura-web---springmvc)
- [json](#cadastro-do-veículo)
- [okhttp](#cadastro-do-veículo)

# Sumário

- [Descrição do Projeto](#descrição-do-projeto)
    - [Primeiro passo](#primeiro-passo)
    - [Segundo passo](#segundo-passo)
    - [Terceiro passo](#terceiro-passo)
- [Primeiros Passos](#primeiros-passos)
  - [Estrutura WEB - SpringMVC](#estrutura-web---springmvc)
  - [Banco de Dados](#banco-de-dados)
      - [Tabela de Usuário (User)](#tabela-de-usuário-user)
      - [Tabela de Veículo (Vehicle)](#tabela-de-veículo-vehicle)
  - [Implementação da Classes (Modelos) User e Vehicle com Hibernate](#implementação-da-classes-modelos-user-e-vehicle-com-hibernate)
    - [@Entity e @Table](#entity-e-table)
    - [@Id e @GeneratedValue](#id-e-generatedvalue)
    - [@OneToMany, @ManyToOne, @JoinColumn](#onetomany-manytoone-joincolumn)
    - [@JsonManagedReference e JsonBackReference](#jsonmanagedreference-e-jsonbackreference)
  - [Jackson](#jackson)
    - [Exemplo](#exemplo)
- [Controladores REST](#controladores-rest)
  - [As anotações do Spring](#as-anotações-do-spring)
    - [@RestController:](#restcontroller)
    - [@Autowired](#autowired)
    - [@RequestMapping](#requestmapping)
    - [@ResponseBody](#responsebody)
    - [@PathVariable](#pathvariable)
- [EndPoints e Resultados](#endpoints-e-resultados)
  - [Cadastro do Usuário:](#cadastro-do-usuário)
  - [Cadastro do Veículo:](#cadastro-do-veículo)
  - [Buscando o Usuário e seus Veículos](#buscando-o-usuário-e-seus-veículos)
- [Conclusão](#conclusão)


# Descrição do Projeto

(Retirado do e-mail enviado ao autor)

Você está fazendo uma API REST que precisará controlar veículos de usuários.

Consiste em três passos cada um como um endpoint do sistema.

### Primeiro passo
> Deve ser a construção de um cadastro de usuários, sendo obrigatórios: nome, e-mail, CPF e data de nascimento, sendo que e-mail e CPF devem ser únicos.

### Segundo passo
> É criar um cadastro de veículos, sendo obrigatórios: Marca, Modelo do Veículo e Ano. E o serviço deve consumir a [API da FIPE](https://deividfortuna.github.io/fipe/) para obter os dados do valor do veículo baseado nas informações inseridas.

### Terceiro passo
> É criar um endpoint que retornará um usuário com a lista de todos seus veículos cadastrados.

No endpoint que listará seus veículos, devemos considerar algumas configurações a serem exibidas para o usuário final. Vamos criar dois novos atributos no objeto do carro, sendo eles:

1. Dia do rodízio deste carro, baseado no último número do ano do veículo, considerando as condicionais:
 - Final 0-1: segunda-feira
 - Final 2-3: terça-feira
 - Final 4-5: quarta-feira
 - Final 6-7: quinta-feira
 - Final 8-9: sexta-feira

2. Também devemos criar um atributo de rodízio ativo, que compara a data atual do sistema com as condicionais anteriores e, quando for o dia ativo do rodizio, retorna **true**; caso contrario, **false**.

> Exemplo A: hoje é segunda-feira, o carro é da marca Fiat, modelo Uno do ano de 2001, ou seja, seu rodízio será às segundas-feiras e o atributo de rodízio ativo será TRUE.

> Exemplo B: hoje é quinta-feira, o carro é da marca Hyundai, modelo HB20 do ano de 2021, ou seja, seu rodizio será às segundas-feiras e o atributo de rodízio ativo será FALSE.

- Caso os cadastros estejam corretos, é necessário voltar o Status 201. Caso hajam erros de preenchimento de dados, o Status deve ser 400.
- Caso a busca esteja correta, é necessário voltar o status 200. Caso haja erro na busca, retornar o status adequado e uma mensagem de erro amigável.

# Primeiros Passos
Para o funcionamento da API precisaremos de duas estruturas principais, a lógica por trás do banco de dados e funcionamento com Hibernate e a estrutura WEB do projeto para os controladores REST.

## Estrutura WEB - SpringMVC

O SpringMVC é um framework que é usado para construir aplicações WEB.
Ele segue o padrão  de desenvolvimento Model-View-Controller. Não veremos aplicações de VIEW neste documento/projeto, mas você pode dar uma olhada no seguinte repositório caso você queira ter maiores conhecimentos na implementações de views com modelos e controladores: [link](https://github.com/gabriel-a-muller/api_rest_test).

Basicamente, neste projeto de exemplo, o SpringMVC torna possível o encaminhamento da URL na chamada WEB para os controladores que estarão responsáveis por aquele mapeamento de URL.

Isto é:

>URL é Chamada -> Servlet Escaneia Componenentes do Projeto -> Encaminha para o Controlador que mapeia a URL

Ou seja, um servlet deve ser criado e o package com do seu projeto deve ser adicionado ao **component-scan**.

Exemplo do projeto:
><context:component-scan base-package="com.vehicleApi" />

Para não estender o tamanho do documento, você pode encontrar os exemplos dessas configs no [repositório](#link-do-repositório) do projeto.

Atente aos arquivos no caminho webapp/WEB-INF/

> web.xml
>
> api-servlet.xml

## Banco de Dados

Antes de partirmos para o desenvolvimento dos controladores REST e a lógica de requisições e respostas, precisamos ter uma plataforma, uma base, para construir nossa aplicação. 

Essa base é o banco de dados no qual as informações serão armazenadas com o auxílio do Hibernate em Java. Para instalação e utilização da tecnologia, precisaremos primeiramente, ter onde e como trabalhar com a ferramenta.

Esta foram as tabelas de banco de dados utilizadas neste sistema:

#### Tabela de Usuário (User)
```
+----------+-------------+------+-----+---------+----------------+
| Field    | Type        | Null | Key | Default | Extra          |
+----------+-------------+------+-----+---------+----------------+
| id       | int         | NO   | PRI | NULL    | auto_increment |
| name     | varchar(60) | NO   |     | NULL    |                |
| birthday | date        | NO   |     | NULL    |                |
| cpf      | varchar(15) | NO   | UNI | NULL    |                |
| email    | varchar(50) | NO   | UNI | NULL    |                |
+----------+-------------+------+-----+---------+----------------+
```

#### Tabela de Veículo (Vehicle)
```
+-----------------+--------------+------+-----+---------+----------------+
| Field           | Type         | Null | Key | Default | Extra          |
+-----------------+--------------+------+-----+---------+----------------+
| id              | int          | NO   | PRI | NULL    | auto_increment |
| user_id         | int          | NO   | MUL | NULL    |                |
| brand           | varchar(25)  | NO   |     | NULL    |                |
| model           | varchar(70)  | NO   |     | NULL    |                |
| year            | int unsigned | NO   |     | NULL    |                |
| price           | varchar(20)  | NO   |     | NULL    |                |
| day_rotation    | int          | NO   |     | NULL    |                |
| rotation_active | tinyint(1)   | NO   |     | NULL    |                |
+-----------------+--------------+------+-----+---------+----------------+
```

## Implementação da Classes (Modelos) User e Vehicle com Hibernate

Com as tabelas criadas no banco de dados à sua escolha, deve-se haver uma maneira de 'traduzir' essa tabela para alguma Classe em Java, isto é, cada objeto instanciado dessa classe se torna um possível canditado a ser inserido (persistido) 'diretamente' no banco de dados (veremos isso depois).

O Hibernate é a tecnologia utilizada neste projeto para essa feature. 

Abaixo é apresentado a implementação da Classe User, que representará a tabela user do banco de dados.

Vale lembrar que a boa prática de programação nos casos de acesso, alteração, remoção e criação desses modelos em banco, é um repositório para cada classe. 

Não irá ser apresentado os repositórios implementados no projeto aqui neste documento, mas você pode verificar a implementação no repositório e até exemplos mais simples na internet que utilizam JPAinterface.


```
@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "user")
	private List<Vehicle> vehicles;
	
	private String name;
	
	private Date birthday;
	
	private String cpf;
	
	private String email;

    //getters and setters
```
```
@Entity
@Table(name = "vehicle)
public class Vehicle {

    ...
    @JsonBackReference
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
    ...

    //getters and setters
}
```

Algumas anotações devem ser explicadas com maiores detalhes neste exemplo.

### @Entity e @Table
Estas anotações no topo da classe alerta ao Hibernate de que a classe deverá ser 'persistida' no banco de dados.

**@Entity** declara à ferramenta que cada objeto dessa classe poderá ser uma nova row (linha) na tabela existente no banco. **@Table** informa ao Hibernate qual será o banco de dados utilizado para esta nova entidade.

### @Id e @GeneratedValue
É padrão que cada entidade persistida em banco tenha um id relacionado a ela. Deve-se anotar o atributo User.**id** com **@Id** juntamente com a lógica de geração desse ID (**@GeneratedValue**). No caso desta implementação os IDs de cada entidade são calculados de maneira crescente no banco de dados utilizado a cada nova adição de dado bem sucedida.

### @OneToMany, @ManyToOne, @JoinColumn
Estas anotações servem para alinhar a lógica da relação entre usuários e veículos.
No caso deste projeto, cada usuário pode ter diversos veículos, portanto, Um Para Vários. Relação que é representada pela anotação **@OneToMany**, o qual é mapeada pela Tabela User.

Na classe User, o atributo User.**vehicles** é do tipo **List\<Vehicle>**, o Hibernate, com a ajuda da anotação relacional, identifica que esse atributo representa outro modelo (no caso, Vehicle).

Para essa relação ser bem sucedida, é necessário adicionar anotações no atributo que representa o usuário dentro da classe Vehicle, a relação de Veículo para Usuário (**@ManyToOne**) e a anotação **@JoinColumn** configura com qual coluna de Vehicle o user.Id (Foreign-Key) estará associado. 

### @JsonManagedReference e JsonBackReference

Estas duas anotações são utilizada pela tecnologia **Jackson**, iremos falar sobre isso no próximo tópico porque essa ferramenta tem uma utilidade muito especial para o funcionamento deste projeto.

Estas duas anotações servem para alertar ao Jackson a relação entre modelos. Caso essas anotações não sejam expecificadas nestes atributos, o Jackson, na hora de converter valores, entrará num looping e não apresentará resultados em Json apropriados.

Desta forma, ambas as notações específicadas no local correto, farão com que o retorno do Jackson esteja sem loopings e erros.

```
Exemplo do Endpoint 3:

"id": 20,
    "vehicles": [
        {
            "id": 9,
            "brand": "VW - VolksWagen",
            "year": 2014,
            "model": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut",
            "price": "R$ 99.197,00",
            "day_rotation": 4,
            "rotation_active": false
        }
    ],
    "name": "Barbara Alexandra",
    "birthday": "1980-04-04",
    "cpf": "0987654321",
    "email": "exemplo_exemplo@exemplo.com.br"
```

## Jackson

A ferramenta Jackson converte o resultado das respostas de Modelos para Json, ou seja, ao retornar um ResponseEntity utilizando algum modelo como parâmetro, o Jackson converte esses valores, com base nos seus atributos, para Json.

Esse processo é chamado de desserializar (deserialize). Processo importante para compor o body da nossa response no formato json de acordo com os dados atribuídos ao modelo.

### Exemplo

```
Dados em Json no POST:

{
    "name": Gabriel,
    "birthday": "1996-30-05",
    "cpf": "123.456.789-10",
    "email": "algum-email@exemplo.com"
}

Método que recebe esse Json convertido para o modelo User.

@PostMapping
public ResponseEntity<?> addUser(@RequestBody User user);
```

Para cada chave no json, o Jackson 'linka' com os atributos do modelo utilizando um construtor default com os getters e setters da classe.

Ao retornar o ResponseEntity tendo um modelo como body, o Jackson faz a conversão contrária, devolvendo os dados em Json.

# Controladores REST

Foram construídos 2 Controladores REST para essa aplicação. 
O **UserRestController** é responsável pelo [**Endpoint 1**](#primeiro-passo) (método **addUser**), que é a criação de novos usuários, e pelo [**Endpoint 3**](#terceiro-passo) (método **getUser**), que é o retorno de algum usuário específico e seus respectivos veículos cadastrados.

```
@RestController
public class UserRestController {

	@Autowired
	UserRepositoryImpl userRepo;

    @RequestMapping(path="/user/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getUser(@PathVariable int id);

    @RequestMapping(path="/user", method = RequestMethod.POST)
	public ResponseEntity<?> addUser(@RequestBody User user);
```

O controlador **VehicleRestController** é responsável por realizar o [**Endpoint 2**](#segundo-passo) (método **addVehicle**), que é o cadastro de veículo com base na utilização da tabela FIPE API.

```
@RestController
public class VehicleRestController {

	@Autowired
	VehicleRepositoryImpl vehicleRepo;

	@Autowired
	UserRepositoryImpl userRepo;

    @RequestMapping(path="/vehicle/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle, @PathVariable int id);
```

## As anotações do Spring

As anotações encontradas neste código fazem parte do Spring Framework.

### @RestController:

> Ao adicionar esta anotação na classe, você específica ao Spring que esta classe representa um controlador Rest.

### @Autowired

> Anotação que é responsável por gerar a injeção de dependência de determinada classe. Neste caso, os repositórios são injetados automaticamente pelo spring porque foi utilizada a anotação para cada um deles.

### @RequestMapping

> Mapeamento da Request. Recebe como parâmetro o caminho (path) e o método (method). Quando alguma request for realizada na determinada URL e método, o controlador irá encaminhar de acordo com o mapeamento.

### @ResponseBody

> O spring recebe o body (corpo) da request e serializa para algum objeto Java, de acordo com o tipo atribuído no parâmetro em seguida.

### @PathVariable

> Se houver alguma variável no caminho da chamada REST, é esta anotação que configura o recebimento dessa variável como parâmetro no método do controlador REST.

# EndPoints e Resultados

Havendo o banco de dados ativo, com os modelos e repositórios em funcionamento com conjunto com o Hibernate.
Com os controladores REST configurados com o SpringFramework, só falta apresentar os métodos dos endpoints e exemplos deles em funcionamento localmente:


## Cadastro do Usuário:

```
@RequestMapping(path="/user", method = RequestMethod.POST)
	public ResponseEntity<?> addUser(@RequestBody User user) {
		
		//Valida CPF
        ...
```
```
		//Verifica se o CPF é único
		if (userRepo.getUserByCpf(user.getCpf()) != null){
			ServerResponse serverResponse = new ServerResponse(user, "This cpf is already registred!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
```
```
		//Verifica se o e-mail é único
		} else if (userRepo.getUserByEmail(user.getEmail()) != null) {
			ServerResponse serverResponse = new ServerResponse(user, "This email is already registred!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
```
```
        //Caso tudo esteja OK com os valores de User, persiste user no banco:
		} else {
			if (userRepo.saveUser(user) != null) {
				ServerResponse serverResponse = new ServerResponse(user, "User created successfuly");
				return new ResponseEntity<>(serverResponse, HttpStatus.CREATED);
			} else {
				ServerResponse serverResponse = new ServerResponse(user, "Server Internal Error!");
				return new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

```

Digamos que a request esteja com os seguintes dados a serem inseridos:
```
POST: localhost:8080/user
{
    "name": "Barbara Alexandra",
    "birthday": "1980-04-05",
    "cpf": "0987654321",
    "email": "exemplo_exemplo@exemplo.com.br"
}
```
A response será a seguinte (com status 201), caso os valores estejam todos válidos:
```
{
    "id": 20,
    "vehicles": null,
    "name": "Barbara Alexandra",
    "birthday": "1980-04-04",
    "cpf": "0987654321",
    "email": "exemplo_exemplo@exemplo.com.br",
    "message": "User created successfuly"
}
```

Perceba que a chave "message" não é nenhum atributo de User. Mas como esse valor está sendo retornado na Response?

O que foi realizado, foi a criação da classe **ServerResponse**, a qual embrulha qualquer modelo e adiciona o atributo **message**

```
public class ServerResponse {
	@JsonUnwrapped
	private final Object wrapped;
	private final String message;
	
	public ServerResponse(Object wrapped, String message) {
		this.wrapped = wrapped;
		this.message = message;
	}

```

Ao retornar **ResponseEntity**, é possível enviar o objeto em conjunto com o **STATUS** da chamada REST. Em caso de sucesso, o resultado será CREATED, **201**. Caso haja qualquer problema no pedido do operador/usuário, na maioria dos casos será retornado o código de BAD_REQUEST, **400**.

## Cadastro do Veículo:

O método de criar novo veículo consiste de diversos métodos auxiliares. Como precisamos acessar a API da FIPE para encontrar o valor do modelo do veículo com base no ano, precisamos estabelecer uma conexão com a API e buscar os valores.

```
private String makeApiRequest(String strRequest) {
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
			.url(strRequest)
			.build();
		Response response = null;

		try {
			response = client.newCall(request).execute();
			String myResult = response.body().string();
			response.body().close();
			return myResult;
		} catch (IOException e) {
		    return "Error!";
		  }
	}
```

Neste método foi utilizado a ferramente **OkHttpClient** para a realização de requisições e obter as respectivas respostas. Ele recebe como parâmetro a URL da API. Essa URL é construída em cada passo de busca de acordo com a documentação da API FIPE.

O primeiro método de busca, é o que busca o código da marca do veículo dentro da API. 

```
private int getBrandCode(String brand) {
		String apiResult = makeApiRequest(getApiUrl());
		// Verificações do corpo da response

        JSONArray apiArray = new JSONArray(apiResult);
        for(int i=0; i < apiArray.length(); i++)   
        {
            JSONObject object = apiArray.getJSONObject(i);
            if (object.getString("nome").equals(brand)) {
                code = Integer.parseInt(object.getString("codigo"));
            }
        }
		return code;
	}
```

Como esta busca retorna um JSON Array, foi-se utilizado a ferramenta **JSON** para serialisar e deserialisar os dados obtidos, ou seja, é contruído um JSONArray com o corpo da resposta, em seguida dentro do For Loop, um JSONObject é criado verificando todos os objetos até que a marca do veículo seja encontrada.

Os outros métodos, como **getModelCode** tem uma lógica semelhante ao **getBrandCode**, a maior diferença consiste em que desta vez, a API retorna um JsonObject composto por uma JsonArray, e não a JsonArray diretamente.

Já o método **getVehiclePrice** recebe um único JsonObject.

O método de criação está apresentado abaixo:

```
@RequestMapping(path="/vehicle/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle, @PathVariable int id) {
		
		User user = userRepo.getUserById(id);
		// Verifica se usuário existe. Caso não exista, retorna código 400.
```
```
		// Método que retorna o código da marca do veículo.
		int code = getBrandCode(vehicle.getBrand());
        // Se o código da marca não for encontrado na API, retorna código 400.
        // Se houver algum problema de conexão com a API, retorna 404.
```
```
        // Método que retorna o código do modelo do veículo com base no código da marca.
        int modelCode = getModelCode(vehicle.getModel(), code);
        // Se o código do modelo não for encontrado na API, retorna código 400.
```
```
        // Adquire o valor do veículo com base no ano e no código do modelo
        String price = getVehiclePrice(modelCode, vehicle.getYear());
        vehicle.setPrice(price);
```
```
		//Seta o dia do rodízio com base no ano.
		vehicle.setDay_rotation(getWeekDay(vehicle.getYear()));
		
		//Seta se o dia do rodízio está ativo no dia de hoje.
		vehicle.setRotation_active(getRotationActive(vehicle.getDay_rotation()));
		
        // Seta o usuário dono do veículo
		vehicle.setUser(user);
```
```
        // Salva veículo no banco de dados.
		if (vehicleRepo.saveVehicle(vehicle) != null) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Vehicle created successfuly!");
			return new ResponseEntity<>(serverResponse, HttpStatus.CREATED);
		} else {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Server Internal Error!");
			return new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
```

Utilizando o Usuário inserido do exemplo anterior, com id 20, foi-se gerado a seguinte POST request:
```
POST: localhost:8080/vehicle/20
{
    "brand": "VW - VolksWagen",
    "model": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut",
    "year": "2014"
}
```
A response será a seguinte (com status 201), caso os valores estejam todos válidos:
```
{
    "id": 9,
    "brand": "VW - VolksWagen",
    "year": 2014,
    "model": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut",
    "price": "R$ 99.197,00",
    "day_rotation": 4,
    "rotation_active": false,
    "message": "Vehicle created successfuly!"
}
```

## Buscando o Usuário e seus Veículos

Graças ao Hibernate, Spring e neste caso mais especial ao Jackson, o terceiro endpoint foi o mais simples de implementar, pois, basta enviar o **id** do usuário na request (localhost:8080/user/20) que o seguinte método será chamado:

```
@RequestMapping(path="/user/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getUser(@PathVariable int id) {
		User user = userRepo.getUserById(id);
		if (user == null) {
			ServerResponse serverResponse = new ServerResponse(user, "User not found!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(user, HttpStatus.OK);
		}
	}
```

Se o Id inserido no caminho da requisição corresponder a algum usuário do sistema, o retorno da API (com status 200) será:

```
{
    "id": 20,
    "vehicles": [
        {
            "id": 9,
            "brand": "VW - VolksWagen",
            "year": 2014,
            "model": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut",
            "price": "R$ 99.197,00",
            "day_rotation": 4,
            "rotation_active": false
        }
    ],
    "name": "Barbara Alexandra",
    "birthday": "1980-04-04",
    "cpf": "0987654321",
    "email": "exemplo_exemplo@exemplo.com.br"
}
```

As ferramentas que instalamos farão basicamente toda a lógica para retornar o que é necessário.
Como o veículo persistido no banco tem um **user_id** atrelado, ao retornar o usuário no corpo da resposta, é convertido em JSON tanto o usuário, como os veículos que pertencem a ele.

Comprovação no banco:

```
Usuário:

 select * from user where id = 20;
+----+-------------------+------------+------------+--------------------------------+
| id | name              | birthday   | cpf        | email                          |
+----+-------------------+------------+------------+--------------------------------+
| 20 | Barbara Alexandra | 1980-04-05 | 0987654321 | exemplo_exemplo@exemplo.com.br |
+----+-------------------+------------+------------+--------------------------------+
```
```
 select user_id, brand, year, price from vehicle where user_id = 20;
+---------+-----------------+------+--------------+
| user_id | brand           | year | price        |
+---------+-----------------+------+--------------+
|      20 | VW - VolksWagen | 2014 | R$ 99.197,00 |
+---------+-----------------+------+--------------+
```

# Conclusão

Na opinião do autor, a API desenvolvida poderia ter melhores práticas de programação, como classes de serviço para os métodos auxiliares e uma tratativa de erros padrão para as classes. No caso, a API funciona e atinge o seu objetivo. Foi útil para prática de algumas ferramentas e trouxe um aprendizado extremamente valioso.

(Conclusão Pessoal - Não técnica)

Como o objetivo do teste era avaliar se o canditado: Consegue aprender, se fazer entender e ainda transmitir conhecimento. Posso dizer com certeza de que aprendi e que no mínimo, estou feliz e agradecido pela oportunidade, o resto, espero que seja bem recebido pelo(s) avaliador(es). Foi desafiador conseguir encaixar a implementação e elaboração do documento no tempo livre durante a semana e final de semana. Mas, fazendo o que a gente ama, as coisas fluem e tiramos a energia necessária.

[Link do Repositório](https://github.com/gabriel-a-muller/api_rest_test)

Caso você tenha interesse em avaliar, existe um repositório de uma implementação front-end em [Spring-MVC](https://github.com/gabriel-a-muller/todo-springmvc).