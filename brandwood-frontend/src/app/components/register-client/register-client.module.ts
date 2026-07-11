import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { RegisterClientComponent } from './register-client.component';

const routes: Routes = [{ path: '', component: RegisterClientComponent }];

@NgModule({
  declarations: [RegisterClientComponent],
  imports: [CommonModule, ReactiveFormsModule, RouterModule.forChild(routes)]
})
export class RegisterClientModule {}
