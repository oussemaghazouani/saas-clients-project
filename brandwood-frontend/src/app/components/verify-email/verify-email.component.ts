import { Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {

  form!: FormGroup;
  email = '';
  loading = false;
  resending = false;
  error = '';
  success = false;

  /** Tableau des 6 digits OTP */
  digits: string[] = ['', '', '', '', '', ''];

  @ViewChildren('digitInput') digitInputs!: QueryList<ElementRef<HTMLInputElement>>;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  devCode = '';

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParams['email'] || '';
    const devCode: string = this.route.snapshot.queryParams['devCode'] || '';
    this.form = this.fb.group({
      email: [this.email, [Validators.required, Validators.email]]
    });
    if (devCode && devCode.length === 6) {
      this.devCode = devCode;
      devCode.split('').forEach((char, i) => { this.digits[i] = char; });
    }
  }

  // ── Gestion des inputs OTP ────────────────────────────────────────────────────

  onDigitInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.replace(/\D/g, '').slice(-1);
    this.digits[index] = value;
    input.value = value;

    if (value && index < 5) {
      const inputs = this.digitInputs.toArray();
      inputs[index + 1].nativeElement.focus();
    }
    if (this.getCode().length === 6) {
      this.submit();
    }
  }

  onDigitKeydown(event: KeyboardEvent, index: number): void {
    if (event.key === 'Backspace') {
      if (!this.digits[index] && index > 0) {
        const inputs = this.digitInputs.toArray();
        this.digits[index - 1] = '';
        inputs[index - 1].nativeElement.focus();
      } else {
        this.digits[index] = '';
      }
    }
    if (event.key === 'ArrowLeft' && index > 0) {
      this.digitInputs.toArray()[index - 1].nativeElement.focus();
    }
    if (event.key === 'ArrowRight' && index < 5) {
      this.digitInputs.toArray()[index + 1].nativeElement.focus();
    }
  }

  onDigitPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const pasted = (event.clipboardData?.getData('text') || '').replace(/\D/g, '').slice(0, 6);
    pasted.split('').forEach((char, i) => { this.digits[i] = char; });
    const inputs = this.digitInputs.toArray();
    const focus = Math.min(pasted.length, 5);
    inputs[focus].nativeElement.focus();
    if (pasted.length === 6) this.submit();
  }

  getCode(): string {
    return this.digits.join('');
  }

  // ── Soumission ────────────────────────────────────────────────────────────────

  submit(): void {
    const code = this.getCode();
    if (code.length !== 6) { this.error = 'Saisissez les 6 chiffres du code.'; return; }

    const emailControl = this.form.get('email');
    if (!emailControl?.valid) { this.error = 'Email invalide.'; return; }

    this.loading = true;
    this.error = '';

    this.authService.verifyEmail({ email: emailControl.value.trim(), code }).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: err => {
        this.error = err.error?.message || 'Code invalide ou expiré.';
        this.loading = false;
        // Efface les digits pour resaisie
        this.digits = ['', '', '', '', '', ''];
        setTimeout(() => this.digitInputs.first?.nativeElement.focus(), 100);
      }
    });
  }

  resendCode(): void {
    const email = this.form.get('email')?.value?.trim();
    if (!email) return;
    this.resending = true;
    this.error = '';
    this.devCode = '';
    this.authService.resendCode(email).subscribe({
      next: (resp: any) => {
        this.resending = false;
        if (resp?.devCode) {
          this.devCode = resp.devCode;
          resp.devCode.split('').forEach((char: string, i: number) => { this.digits[i] = char; });
        }
      },
      error: err => {
        this.error = err.error?.message || 'Impossible de renvoyer le code.';
        this.resending = false;
      }
    });
  }
}
