import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

/** Validateur personnalisé : les deux mots de passe doivent correspondre. */
function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-register-client',
  templateUrl: './register-client.component.html'
})
export class RegisterClientComponent implements OnInit {

  form!: FormGroup;
  loading = false;
  error = '';
  success = false;
  showPassword = false;
  showConfirm = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName:       ['', [Validators.required, Validators.maxLength(100)]],
      lastName:        ['', [Validators.required, Validators.maxLength(100)]],
      email:           ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
      phone:           ['', Validators.maxLength(30)],
      companyName:     ['', [Validators.required, Validators.maxLength(150)]],
      password:        ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordMatchValidator });
  }

  get f() { return this.form.controls; }

  get passwordStrength(): number {
    const pw: string = this.f['password'].value || '';
    let score = 0;
    if (pw.length >= 8)  score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[a-z]/.test(pw)) score++;
    if (/\d/.test(pw))    score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    return score;
  }

  get strengthLabel(): string {
    const labels = ['', 'Très faible', 'Faible', 'Moyen', 'Fort', 'Très fort'];
    return labels[this.passwordStrength];
  }

  get strengthColor(): string {
    const colors = ['', '#dc3545', '#fd7e14', '#ffc107', '#20c997', '#198754'];
    return colors[this.passwordStrength];
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.error = '';

    this.authService.registerClient({
      firstName:   this.f['firstName'].value.trim(),
      lastName:    this.f['lastName'].value.trim(),
      email:       this.f['email'].value.trim(),
      password:    this.f['password'].value,
      phone:       this.f['phone'].value?.trim() || undefined,
      companyName: this.f['companyName'].value.trim()
    }).subscribe({
      next: (resp: any) => {
        this.success = true;
        this.loading = false;
        const params: Record<string, string> = { email: this.f['email'].value.trim() };
        if (resp?.devCode) { params['devCode'] = resp.devCode; }
        setTimeout(() => this.router.navigate(['/verify-email'], { queryParams: params }), 2000);
      },
      error: err => {
        this.error = err.error?.message || 'Une erreur est survenue. Veuillez réessayer.';
        this.loading = false;
      }
    });
  }
}
