import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { RegisterPrestataireComponent } from './register-prestataire.component';

const routes: Routes = [{ path: '', component: RegisterPrestataireComponent }];

@NgModule({
  declarations: [RegisterPrestataireComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class RegisterPrestataireModule {}
