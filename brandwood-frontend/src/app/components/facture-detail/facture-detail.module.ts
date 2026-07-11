import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FactureDetailComponent } from './facture-detail.component';

const routes: Routes = [{ path: '', component: FactureDetailComponent }];

@NgModule({
  declarations: [FactureDetailComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class FactureDetailModule {}
