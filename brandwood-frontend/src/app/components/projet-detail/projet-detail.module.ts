import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { ProjetDetailComponent } from './projet-detail.component';

const routes: Routes = [{ path: '', component: ProjetDetailComponent }];

@NgModule({
  declarations: [ProjetDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class ProjetDetailModule {}
