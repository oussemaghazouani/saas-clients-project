import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('newPassword')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {

  form!: FormGroup;
  token = '';
  loading = false;
  success = false;
  error = '';
  showNew = false;
  showConfirm = false;
  invalidToken = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (!this.token) { this.invalidToken = true; }

    this.form = this.fb.group({
      newPassword:     ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordMatchValidator });
  }

  get f() { return this.form.controls; }

  get passwordStrength(): number {
    const pw: string = this.f['newPassword'].value || '';
    let s = 0;
    if (pw.length >= 8)           s++;
    if (/[A-Z]/.test(pw))         s++;
    if (/[a-z]/.test(pw))         s++;
    if (/\d/.test(pw))            s++;
    if (/[^A-Za-z0-9]/.test(pw))  s++;
    return s;
  }

  get strengthLabel(): string {
    return ['', 'Très faible', 'Faible', 'Moyen', 'Fort', 'Très fort'][this.passwordStrength];
  }

  get strengthColor(): string {
    return ['', '#dc3545', '#fd7e14', '#ffc107', '#20c997', '#198754'][this.passwordStrength];
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';

    this.authService.resetPassword({
      token:       this.token,
      newPassword: this.f['newPassword'].value
    }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: err => {
        this.error = err.error?.message || 'Lien invalide ou expiré.';
        this.loading = false;
      }
    });
  }
}
