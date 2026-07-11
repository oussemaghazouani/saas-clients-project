import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { StatistiquesComponent } from './statistiques.component';

const routes: Routes = [{ path: '', component: StatistiquesComponent }];

@NgModule({
  declarations: [StatistiquesComponent],
  imports: [CommonModule, SharedModule, RouterModule.forChild(routes)]
})
export class StatistiquesModule {}
