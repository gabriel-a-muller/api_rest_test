# Implementa��o de uma API em Java com Hibernate e Spring

#### [Link do Reposit�rio](https://github.com/gabriel-a-muller/api_rest_test)

Ol�, esse documento consiste na explica��o da implementa��o da API *FIPE HTTP REST* passo-a-passo.

Ir� ser apresentado as tecnologias utilizadas, a justificativa de cada uma e poss�veis c�digos exemplos que possam esclarecer o uso das ferramentas na pr�tica.

O que voc� precisa saber ao ler este documento:

- Java (Para compreens�o de classes e tipos de vari�vel)

O que voc� aprender� ao ler este documento:

- Utilizar o Hibernate para aquisi��o e persist�ncia de modelos com o banco de dados.
- Cria��o de Controladores Rest com Spring-Framework.
- Utilizar GET e POST com os controladores.
- Enviar e receber dados json com Jackson. 
- Personalizar mensagem de status e o pr�prio c�digo de status da resposta HTML.

O que voc� N�O aprender� ao ler este documento:

- Como criar um banco de dados e a tabela para cada modelo.
- Como instalar cada ferramenta.

Tecnologias Utilizadas:

- [hibernate-core](#implementa��o-da-classes-modelos-user-e-vehicle-com-hibernate)
- [mysql-connector-java](#implementa��o-da-classes-modelos-user-e-vehicle-com-hibernate)
- [jackson-databind](#jackson)
- [spring-webmvc](#estrutura-web---springmvc)
- [json](#cadastro-do-ve�culo)
- [okhttp](#cadastro-do-ve�culo)

# Sum�rio

- [Descri��o do Projeto](#descri��o-do-projeto)
    - [Primeiro passo](#primeiro-passo)
    - [Segundo passo](#segundo-passo)
    - [Terceiro passo](#terceiro-passo)
- [Primeiros Passos](#primeiros-passos)
  - [Estrutura WEB - SpringMVC](#estrutura-web---springmvc)
  - [Banco de Dados](#banco-de-dados)
      - [Tabela de Usu�rio (User)](#tabela-de-usu�rio-user)
      - [Tabela de Ve�culo (Vehicle)](#tabela-de-ve�culo-vehicle)
  - [Implementa��o da Classes (Modelos) User e Vehicle com Hibernate](#implementa��o-da-classes-modelos-user-e-vehicle-com-hibernate)
    - [@Entity e @Table](#entity-e-table)
    - [@Id e @GeneratedValue](#id-e-generatedvalue)
    - [@OneToMany, @ManyToOne, @JoinColumn](#onetomany-manytoone-joincolumn)
    - [@JsonManagedReference e JsonBackReference](#jsonmanagedreference-e-jsonbackreference)
  - [Jackson](#jackson)
    - [Exemplo](#exemplo)
- [Controladores REST](#controladores-rest)
  - [As anota��es do Spring](#as-anota��es-do-spring)
    - [@RestController:](#restcontroller)
    - [@Autowired](#autowired)
    - [@RequestMapping](#requestmapping)
    - [@ResponseBody](#responsebody)
    - [@PathVariable](#pathvariable)
- [EndPoints e Resultados](#endpoints-e-resultados)
  - [Cadastro do Usu�rio:](#cadastro-do-usu�rio)
  - [Cadastro do Ve�culo:](#cadastro-do-ve�culo)
  - [Buscando o Usu�rio e seus Ve�culos](#buscando-o-usu�rio-e-seus-ve�culos)
- [Conclus�o](#conclus�o)


# Descri��o do Projeto

(Retirado do e-mail enviado ao autor)

Voc� est� fazendo uma API REST que precisar� controlar ve�culos de usu�rios.

Consiste em tr�s passos cada um como um endpoint do sistema.

### Primeiro passo
> Deve ser a constru��o de um cadastro de usu�rios, sendo obrigat�rios: nome, e-mail, CPF e data de nascimento, sendo que e-mail e CPF devem ser �nicos.

### Segundo passo
> � criar um cadastro de ve�culos, sendo obrigat�rios: Marca, Modelo do Ve�culo e Ano. E o servi�o deve consumir a [API da FIPE](https://deividfortuna.github.io/fipe/) para obter os dados do valor do ve�culo baseado nas informa��es inseridas.

### Terceiro passo
> � criar um endpoint que retornar� um usu�rio com a lista de todos seus ve�culos cadastrados.

No endpoint que listar� seus ve�culos, devemos considerar algumas configura��es a serem exibidas para o usu�rio final. Vamos criar dois novos atributos no objeto do carro, sendo eles:

1. Dia do rod�zio deste carro, baseado no �ltimo n�mero do ano do ve�culo, considerando as condicionais:
 - Final 0-1: segunda-feira
 - Final 2-3: ter�a-feira
 - Final 4-5: quarta-feira
 - Final 6-7: quinta-feira
 - Final 8-9: sexta-feira

2. Tamb�m devemos criar um atributo de rod�zio ativo, que compara a data atual do sistema com as condicionais anteriores e, quando for o dia ativo do rodizio, retorna **true**; caso contrario, **false**.

> Exemplo A: hoje � segunda-feira, o carro � da marca Fiat, modelo Uno do ano de 2001, ou seja, seu rod�zio ser� �s segundas-feiras e o atributo de rod�zio ativo ser� TRUE.

> Exemplo B: hoje � quinta-feira, o carro � da marca Hyundai, modelo HB20 do ano de 2021, ou seja, seu rodizio ser� �s segundas-feiras e o atributo de rod�zio ativo ser� FALSE.

- Caso os cadastros estejam corretos, � necess�rio voltar o Status 201. Caso hajam erros de preenchimento de dados, o Status deve ser 400.
- Caso a busca esteja correta, � necess�rio voltar o status 200. Caso haja erro na busca, retornar o status adequado e uma mensagem de erro amig�vel.

# Primeiros Passos
Para o funcionamento da API precisaremos de duas estruturas principais, a l�gica por tr�s do banco de dados e funcionamento com Hibernate e a estrutura WEB do projeto para os controladores REST.

## Estrutura WEB - SpringMVC

O SpringMVC � um framework que � usado para construir aplica��es WEB.
Ele segue o padr�o  de desenvolvimento Model-View-Controller. N�o veremos aplica��es de VIEW neste documento/projeto, mas voc� pode dar uma olhada no seguinte reposit�rio caso voc� queira ter maiores conhecimentos na implementa��es de views com modelos e controladores: [link](https://github.com/gabriel-a-muller/api_rest_test).

Basicamente, neste projeto de exemplo, o SpringMVC torna poss�vel o encaminhamento da URL na chamada WEB para os controladores que estar�o respons�veis por aquele mapeamento de URL.

Isto �:

>URL � Chamada -> Servlet Escaneia Componenentes do Projeto -> Encaminha para o Controlador que mapeia a URL

Ou seja, um servlet deve ser criado e o package com do seu projeto deve ser adicionado ao **component-scan**.

Exemplo do projeto:
><context:component-scan base-package="com.vehicleApi" />

Para n�o estender o tamanho do documento, voc� pode encontrar os exemplos dessas configs no [reposit�rio](#link-do-reposit�rio) do projeto.

Atente aos arquivos no caminho webapp/WEB-INF/

> web.xml
>
> api-servlet.xml

## Banco de Dados

Antes de partirmos para o desenvolvimento dos controladores REST e a l�gica de requisi��es e respostas, precisamos ter uma plataforma, uma base, para construir nossa aplica��o. 

Essa base � o banco de dados no qual as informa��es ser�o armazenadas com o aux�lio do Hibernate em Java. Para instala��o e utiliza��o da tecnologia, precisaremos primeiramente, ter onde e como trabalhar com a ferramenta.

Esta foram as tabelas de banco de dados utilizadas neste sistema:

#### Tabela de Usu�rio (User)
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

#### Tabela de Ve�culo (Vehicle)
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

## Implementa��o da Classes (Modelos) User e Vehicle com Hibernate

Com as tabelas criadas no banco de dados � sua escolha, deve-se haver uma maneira de 'traduzir' essa tabela para alguma Classe em Java, isto �, cada objeto instanciado dessa classe se torna um poss�vel canditado a ser inserido (persistido) 'diretamente' no banco de dados (veremos isso depois).

O Hibernate � a tecnologia utilizada neste projeto para essa feature. 

Abaixo � apresentado a implementa��o da Classe User, que representar� a tabela user do banco de dados.

Vale lembrar que a boa pr�tica de programa��o nos casos de acesso, altera��o, remo��o e cria��o desses modelos em banco, � um reposit�rio para cada classe. 

N�o ir� ser apresentado os reposit�rios implementados no projeto aqui neste documento, mas voc� pode verificar a implementa��o no reposit�rio e at� exemplos mais simples na internet que utilizam JPAinterface.


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

Algumas anota��es devem ser explicadas com maiores detalhes neste exemplo.

### @Entity e @Table
Estas anota��es no topo da classe alerta ao Hibernate de que a classe dever� ser 'persistida' no banco de dados.

**@Entity** declara � ferramenta que cada objeto dessa classe poder� ser uma nova row (linha) na tabela existente no banco. **@Table** informa ao Hibernate qual ser� o banco de dados utilizado para esta nova entidade.

### @Id e @GeneratedValue
� padr�o que cada entidade persistida em banco tenha um id relacionado a ela. Deve-se anotar o atributo User.**id** com **@Id** juntamente com a l�gica de gera��o desse ID (**@GeneratedValue**). No caso desta implementa��o os IDs de cada entidade s�o calculados de maneira crescente no banco de dados utilizado a cada nova adi��o de dado bem sucedida.

### @OneToMany, @ManyToOne, @JoinColumn
Estas anota��es servem para alinhar a l�gica da rela��o entre usu�rios e ve�culos.
No caso deste projeto, cada usu�rio pode ter diversos ve�culos, portanto, Um Para V�rios. Rela��o que � representada pela anota��o **@OneToMany**, o qual � mapeada pela Tabela User.

Na classe User, o atributo User.**vehicles** � do tipo **List\<Vehicle>**, o Hibernate, com a ajuda da anota��o relacional, identifica que esse atributo representa outro modelo (no caso, Vehicle).

Para essa rela��o ser bem sucedida, � necess�rio adicionar anota��es no atributo que representa o usu�rio dentro da classe Vehicle, a rela��o de Ve�culo para Usu�rio (**@ManyToOne**) e a anota��o **@JoinColumn** configura com qual coluna de Vehicle o user.Id (Foreign-Key) estar� associado. 

### @JsonManagedReference e JsonBackReference

Estas duas anota��es s�o utilizada pela tecnologia **Jackson**, iremos falar sobre isso no pr�ximo t�pico porque essa ferramenta tem uma utilidade muito especial para o funcionamento deste projeto.

Estas duas anota��es servem para alertar ao Jackson a rela��o entre modelos. Caso essas anota��es n�o sejam expecificadas nestes atributos, o Jackson, na hora de converter valores, entrar� num looping e n�o apresentar� resultados em Json apropriados.

Desta forma, ambas as nota��es espec�ficadas no local correto, far�o com que o retorno do Jackson esteja sem loopings e erros.

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

A ferramenta Jackson converte o resultado das respostas de Modelos para Json, ou seja, ao retornar um ResponseEntity utilizando algum modelo como par�metro, o Jackson converte esses valores, com base nos seus atributos, para Json.

Esse processo � chamado de desserializar (deserialize). Processo importante para compor o body da nossa response no formato json de acordo com os dados atribu�dos ao modelo.

### Exemplo

```
Dados em Json no POST:

{
    "name": Gabriel,
    "birthday": "1996-30-05",
    "cpf": "123.456.789-10",
    "email": "algum-email@exemplo.com"
}

M�todo que recebe esse Json convertido para o modelo User.

@PostMapping
public ResponseEntity<?> addUser(@RequestBody User user);
```

Para cada chave no json, o Jackson 'linka' com os atributos do modelo utilizando um construtor default com os getters e setters da classe.

Ao retornar o ResponseEntity tendo um modelo como body, o Jackson faz a convers�o contr�ria, devolvendo os dados em Json.

# Controladores REST

Foram constru�dos 2 Controladores REST para essa aplica��o. 
O **UserRestController** � respons�vel pelo [**Endpoint 1**](#primeiro-passo) (m�todo **addUser**), que � a cria��o de novos usu�rios, e pelo [**Endpoint 3**](#terceiro-passo) (m�todo **getUser**), que � o retorno de algum usu�rio espec�fico e seus respectivos ve�culos cadastrados.

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

O controlador **VehicleRestController** � respons�vel por realizar o [**Endpoint 2**](#segundo-passo) (m�todo **addVehicle**), que � o cadastro de ve�culo com base na utiliza��o da tabela FIPE API.

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

## As anota��es do Spring

As anota��es encontradas neste c�digo fazem parte do Spring Framework.

### @RestController:

> Ao adicionar esta anota��o na classe, voc� espec�fica ao Spring que esta classe representa um controlador Rest.

### @Autowired

> Anota��o que � respons�vel por gerar a inje��o de depend�ncia de determinada classe. Neste caso, os reposit�rios s�o injetados automaticamente pelo spring porque foi utilizada a anota��o para cada um deles.

### @RequestMapping

> Mapeamento da Request. Recebe como par�metro o caminho (path) e o m�todo (method). Quando alguma request for realizada na determinada URL e m�todo, o controlador ir� encaminhar de acordo com o mapeamento.

### @ResponseBody

> O spring recebe o body (corpo) da request e serializa para algum objeto Java, de acordo com o tipo atribu�do no par�metro em seguida.

### @PathVariable

> Se houver alguma vari�vel no caminho da chamada REST, � esta anota��o que configura o recebimento dessa vari�vel como par�metro no m�todo do controlador REST.

# EndPoints e Resultados

Havendo o banco de dados ativo, com os modelos e reposit�rios em funcionamento com conjunto com o Hibernate.
Com os controladores REST configurados com o SpringFramework, s� falta apresentar os m�todos dos endpoints e exemplos deles em funcionamento localmente:


## Cadastro do Usu�rio:

```
@RequestMapping(path="/user", method = RequestMethod.POST)
	public ResponseEntity<?> addUser(@RequestBody User user) {
		
		//Valida CPF
        ...
```
```
		//Verifica se o CPF � �nico
		if (userRepo.getUserByCpf(user.getCpf()) != null){
			ServerResponse serverResponse = new ServerResponse(user, "This cpf is already registred!");
			return new ResponseEntity<>(serverResponse, HttpStatus.BAD_REQUEST);
```
```
		//Verifica se o e-mail � �nico
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
A response ser� a seguinte (com status 201), caso os valores estejam todos v�lidos:
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

Perceba que a chave "message" n�o � nenhum atributo de User. Mas como esse valor est� sendo retornado na Response?

O que foi realizado, foi a cria��o da classe **ServerResponse**, a qual embrulha qualquer modelo e adiciona o atributo **message**

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

Ao retornar **ResponseEntity**, � poss�vel enviar o objeto em conjunto com o **STATUS** da chamada REST. Em caso de sucesso, o resultado ser� CREATED, **201**. Caso haja qualquer problema no pedido do operador/usu�rio, na maioria dos casos ser� retornado o c�digo de BAD_REQUEST, **400**.

## Cadastro do Ve�culo:

O m�todo de criar novo ve�culo consiste de diversos m�todos auxiliares. Como precisamos acessar a API da FIPE para encontrar o valor do modelo do ve�culo com base no ano, precisamos estabelecer uma conex�o com a API e buscar os valores.

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

Neste m�todo foi utilizado a ferramente **OkHttpClient** para a realiza��o de requisi��es e obter as respectivas respostas. Ele recebe como par�metro a URL da API. Essa URL � constru�da em cada passo de busca de acordo com a documenta��o da API FIPE.

O primeiro m�todo de busca, � o que busca o c�digo da marca do ve�culo dentro da API. 

```
private int getBrandCode(String brand) {
		String apiResult = makeApiRequest(getApiUrl());
		// Verifica��es do corpo da response

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

Como esta busca retorna um JSON Array, foi-se utilizado a ferramenta **JSON** para serialisar e deserialisar os dados obtidos, ou seja, � contru�do um JSONArray com o corpo da resposta, em seguida dentro do For Loop, um JSONObject � criado verificando todos os objetos at� que a marca do ve�culo seja encontrada.

Os outros m�todos, como **getModelCode** tem uma l�gica semelhante ao **getBrandCode**, a maior diferen�a consiste em que desta vez, a API retorna um JsonObject composto por uma JsonArray, e n�o a JsonArray diretamente.

J� o m�todo **getVehiclePrice** recebe um �nico JsonObject.

O m�todo de cria��o est� apresentado abaixo:

```
@RequestMapping(path="/vehicle/{id}", method = RequestMethod.POST)
	public ResponseEntity<?> addVehicle(@RequestBody Vehicle vehicle, @PathVariable int id) {
		
		User user = userRepo.getUserById(id);
		// Verifica se usu�rio existe. Caso n�o exista, retorna c�digo 400.
```
```
		// M�todo que retorna o c�digo da marca do ve�culo.
		int code = getBrandCode(vehicle.getBrand());
        // Se o c�digo da marca n�o for encontrado na API, retorna c�digo 400.
        // Se houver algum problema de conex�o com a API, retorna 404.
```
```
        // M�todo que retorna o c�digo do modelo do ve�culo com base no c�digo da marca.
        int modelCode = getModelCode(vehicle.getModel(), code);
        // Se o c�digo do modelo n�o for encontrado na API, retorna c�digo 400.
```
```
        // Adquire o valor do ve�culo com base no ano e no c�digo do modelo
        String price = getVehiclePrice(modelCode, vehicle.getYear());
        vehicle.setPrice(price);
```
```
		//Seta o dia do rod�zio com base no ano.
		vehicle.setDay_rotation(getWeekDay(vehicle.getYear()));
		
		//Seta se o dia do rod�zio est� ativo no dia de hoje.
		vehicle.setRotation_active(getRotationActive(vehicle.getDay_rotation()));
		
        // Seta o usu�rio dono do ve�culo
		vehicle.setUser(user);
```
```
        // Salva ve�culo no banco de dados.
		if (vehicleRepo.saveVehicle(vehicle) != null) {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Vehicle created successfuly!");
			return new ResponseEntity<>(serverResponse, HttpStatus.CREATED);
		} else {
			ServerResponse serverResponse = new ServerResponse(vehicle, "Server Internal Error!");
			return new ResponseEntity<>(serverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
```

Utilizando o Usu�rio inserido do exemplo anterior, com id 20, foi-se gerado a seguinte POST request:
```
POST: localhost:8080/vehicle/20
{
    "brand": "VW - VolksWagen",
    "model": "AMAROK High.CD 2.0 16V TDI 4x4 Dies. Aut",
    "year": "2014"
}
```
A response ser� a seguinte (com status 201), caso os valores estejam todos v�lidos:
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

## Buscando o Usu�rio e seus Ve�culos

Gra�as ao Hibernate, Spring e neste caso mais especial ao Jackson, o terceiro endpoint foi o mais simples de implementar, pois, basta enviar o **id** do usu�rio na request (localhost:8080/user/20) que o seguinte m�todo ser� chamado:

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

Se o Id inserido no caminho da requisi��o corresponder a algum usu�rio do sistema, o retorno da API (com status 200) ser�:

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

As ferramentas que instalamos far�o basicamente toda a l�gica para retornar o que � necess�rio.
Como o ve�culo persistido no banco tem um **user_id** atrelado, ao retornar o usu�rio no corpo da resposta, � convertido em JSON tanto o usu�rio, como os ve�culos que pertencem a ele.

Comprova��o no banco:

```
Usu�rio:

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

# Conclus�o

Na opini�o do autor, a API desenvolvida poderia ter melhores pr�ticas de programa��o, como classes de servi�o para os m�todos auxiliares e uma tratativa de erros padr�o para as classes. No caso, a API funciona e atinge o seu objetivo. Foi �til para pr�tica de algumas ferramentas e trouxe um aprendizado extremamente valioso.

(Conclus�o Pessoal - N�o t�cnica)

Como o objetivo do teste era avaliar se o canditado: Consegue aprender, se fazer entender e ainda transmitir conhecimento. Posso dizer com certeza de que aprendi e que no m�nimo, estou feliz e agradecido pela oportunidade, o resto, espero que seja bem recebido pelo(s) avaliador(es). Foi desafiador conseguir encaixar a implementa��o e elabora��o do documento no tempo livre durante a semana e final de semana. Mas, fazendo o que a gente ama, as coisas fluem e tiramos a energia necess�ria.

[Link do Reposit�rio](https://github.com/gabriel-a-muller/api_rest_test)

Caso voc� tenha interesse em avaliar, existe um reposit�rio de uma implementa��o front-end em [Spring-MVC](https://github.com/gabriel-a-muller/todo-springmvc).