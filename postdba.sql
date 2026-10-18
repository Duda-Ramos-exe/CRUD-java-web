
CREATE TABLE usuario(
	id serial PRIMARY KEY,
	nome varchar(100) not null,
	datanascimento date not null,
	cpf varchar(11) unique not null,
	email varchar(100) unique not null,
	senha varchar(20) not null,
	salario decimal(15,2) not null
);
