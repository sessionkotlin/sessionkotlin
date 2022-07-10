# SMTP client

## Usage

1. Edit the file [client/mail.env.template](client/mail.env.template) and insert:
    1. The hostname of an SMTP server;
    2. The port of the SMTP server;
    3. The SMTP client domain;
    4. The sender's email, which will be used for authentication as well;
    5. The password for the previous email;
    6. The recipient's email.


2. Rename the same file to `mail.env`;


3. Execute the client
    ```shell
    ./gradlew client:run
    ```
   The interactions will be logged to the console.

## Notes:

> 504 Requested authentication method is invalid

The SMTP server used does not support LOGIN authentication.

---

> 534 Application-specific password required

The SMTP server requires an app specific password instead of the password of the account.

For gmail, check out this
[link](https://support.google.com/accounts/answer/185833?&p=InvalidSecondFactor).

---

> 535 Authentication unsuccessful

Either the sender/password combination is incorrect or the SMTP server
refuses to authenticate the client.

For example, Gmail will respond with 535 when attempting to log in with the account's password
and the account does not have two-factor activated.

To fix this, enable 2FA and create
an [App Password](https://support.google.com/accounts/answer/185833?&p=InvalidSecondFactor).
