import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('password')?.value;
  const confirm = control.get('confirmPassword')?.value;
  return pw && confirm && pw !== confirm ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-register-prestataire',
  templateUrl: './register-prestataire.component.html'
})
export class RegisterPrestataireComponent implements OnInit {

  form!: FormGroup;
  loading = false;
  error = '';
  success = false;
  showPassword = false;
  showConfirm = false;

  specialites = [
    'Développement Web', 'Développement Mobile', 'Design UI/UX',
    'Marketing Digital', 'SEO / SEA', 'Rédaction de contenu',
    'Community Management', 'Graphisme', 'Vidéo & Motion Design', 'Autre'
  ];

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
      jobTitle:        ['', [Validators.required, Validators.maxLength(150)]],
      companyName:     ['', Validators.maxLength(150)],
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

    this.authService.registerPrestataire({
      firstName:   this.f['firstName'].value.trim(),
      lastName:    this.f['lastName'].value.trim(),
      email:       this.f['email'].value.trim(),
      password:    this.f['password'].value,
      phone:       this.f['phone'].value?.trim() || undefined,
      jobTitle:    this.f['jobTitle'].value.trim(),
      companyName: this.f['companyName'].value?.trim() || undefined
    }).subscribe({
      next: (resp: any) => {
        this.success = true;
        this.loading = false;
        const params: Record<string, string> = { email: this.f['email'].value.trim() };
        if (resp?.devCode) { params['devCode'] = resp.devCode; }
        setTimeout(() => this.router.navigate(['/verify-email'], { queryParams: params }), 2000);
      },
      error: err => {
        this.error = err.error?.message || 'Une erreur est survenue.';
        this.loading = false;
      }
    });
  }
}
