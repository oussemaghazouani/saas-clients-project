import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { ProjetsComponent } from './projets.component';

const routes: Routes = [{ path: '', component: ProjetsComponent }];

@NgModule({
  declarations: [ProjetsComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class ProjetsModule {}
