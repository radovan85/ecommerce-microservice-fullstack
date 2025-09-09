import { Component, inject } from '@angular/core';
import { AuthenticationRequest } from '../../classes/authentication-request';
import { AuthService } from '../../services/auth.service';
import { User } from '../../classes/user';
import axios from 'axios';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {


  private authRequest: AuthenticationRequest = new AuthenticationRequest;
  private authService = inject(AuthService);

  ngAfterViewInit(): void {
    this.executeLoginForm();
  }

  public getAuthRequest(): AuthenticationRequest {
    return this.authRequest;
  }

  executeLoginForm() {
    var form = document.getElementById(`loginForm`) as HTMLFormElement;
    var alertMessage = document.getElementById(`error-message`);
    var authUser: User = new User;
    form.addEventListener(`submit`, async (event) => {
      event.preventDefault();

      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });


      await axios.post(`${this.authService.getTargetUrl()}api/auth/login`, {
        username: serializedData[`email`],
        password: serializedData[`password`]
      })

        .then((response) => {
          localStorage.setItem(`currentUser`, JSON.stringify(response));
          authUser = response.data;
          var tokenStr = authUser.authToken;
          var authToken = '';
          if (tokenStr) {
            authToken = `Bearer ${tokenStr}`;
            localStorage.setItem(`authToken`, authToken);
            var rolesIds = authUser.rolesIds;
            if (rolesIds) {
              var roleId = Object.values(rolesIds)[0];
              if (roleId === 1) {
                localStorage.setItem(`role`, `ADMIN`);
              }

              if (roleId === 2) {
                localStorage.setItem(`role`, `ROLE_USER`);
              }
            }
          }

          if (alertMessage) {
            alertMessage.style.visibility = `hidden`;
          }



          console.log(`Login completed!`);
          window.location.reload();

        })

        .catch(() => {
          if (alertMessage) {
            alertMessage.style.visibility = `visible`;
          }
        })
    });
  }


}