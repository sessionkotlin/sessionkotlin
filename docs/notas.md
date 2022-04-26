- Não dá para processar o tipo global no processador de anotações porque estas são processadas num stage anterior e só
  tem acesso aos símbolos.


- Como a declaração do protocolo global tem de correr antes para a API ser gerada, temos de separar em dois módulos do
  gradle. Cada módulo tem tasks independentes e podem depender um do outro.
  Senão, para gerar a API teria-se de apagar/comentar os usos da API antiga.


- Separar o backend de comunicação num módulo/library e testar isoladamente.


- Se tanto o backend de comunicação, como a DSL (definição protocolo global) e a geração da API forem testados
  individualmente, basta criar um exemplo grande de um projeto que usa tudo (uma espécie de "demo" ou "template").


- Ktor's sockets como um dos backends de comunicação (para além dos canais). Oferecem uma API assíncrona para
  envio/receção.
  O canal de escrita tem autoFlush=true porque as mensagens são muito pequenas.


- É possível misturar sockets e channels usando a mesma sintaxe (suspending style).


- Os canais têm buffer "ilimitado". A escrita é assíncrona, e a leitura pode suspender.


- O scribble usa labels nas mensagens para desambiguar, e faz once-unfolding.
  Aqui, as escolhas são materializadas numa mensagem.


- A linearidade é protegida em runtime: cada endpoint só pode ser usado uma vez.
  Se o utilizador tentar usar uma segunda vez, ocorre uma exceção.


- Tanto os canais como os sockets são fechados automaticamente.
  Quando alguem tenta ler de uma conexão fechada é lançada logo uma exceção.
  Assim, se algum participante não terminar o protocolo, os outros não ficam à espera.


- É possível criar um SKChannel definindo apenas um ou nenhum dos participantes,
adiando a sua definição até à tentativa de uso.


- Para gerar a API de callbacks, as mensagens têm de ser anotadas com uma label, usada no nome do método.
