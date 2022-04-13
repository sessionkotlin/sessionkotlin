- Não dá para processar o tipo global no processador de anotações porque estas são processadas num stage anterior e só
  tem acesso aos símbolos.


- Como a declaração do protocolo global tem de correr antes, para a API ser gerada, temos de separar em dois módulos do
  gradle. Cada módulo tem tasks independentes e podem depender um do outro.


- Separar o backend de comunicação num módulo/library e testar isoladamente.


- Se tanto o backend de comunicação, como a DSL (definição protocolo global) e a geração da API forem testados
  individualmente, basta criar um exemplo grande de um projeto que usa tudo (uma espécie de "demo" ou "template").


- Ktor's sockets como um dos backends de comunicação (para além dos canais). Oferecem uma API assíncrona para envio/receção.
  O canal de escrita tem autoFlush=true porque as mensagens são muito pequenas.


- É possível misturar sockets e channels usando a mesma sintaxe (suspending style).


- Os canais têm buffer "ilimitado". A escrita é assíncrona, e a leitura pode suspender.
