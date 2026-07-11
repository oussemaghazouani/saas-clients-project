import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent implements OnInit {

  form!: FormGroup;
  loading = false;
  submitted = false;
  error = '';

  constructor(private fb: FormBuilder, private authService: AuthService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get f() { return this.form.controls; }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';

    this.authService.forgotPassword({ email: this.f['email'].value.trim() }).subscribe({
      next: () => {
        this.submitted = true;
        this.loading = false;
      },
      error: err => {
        // On affiche toujours le message de succès générique pour ne pas révéler
        // si l'email existe (même en cas d'erreur réseau on peut rester vague).
        this.submitted = true;
        this.loading = false;
      }
    });
  }
}
