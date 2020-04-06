package com.sanda.truckdoc.client.wizard;

import android.content.Context;
import android.content.res.Resources;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.wizard.wizardpager.model.AbstractWizardModel;
import com.sanda.truckdoc.client.wizard.wizardpager.model.BranchPage;
import com.sanda.truckdoc.client.wizard.wizardpager.model.ImagesChoicePage;
import com.sanda.truckdoc.client.wizard.wizardpager.model.PageList;
import com.sanda.truckdoc.client.wizard.wizardpager.model.TextNotesPage;

public class AccidentModel extends AbstractWizardModel {

    public AccidentModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        Resources resources = mContext.getApplicationContext().getResources();
        return new PageList(

                //
                new ImagesChoicePage(this, "общий план", resources.getString(R.string.acc_common_scene)), //
                new ImagesChoicePage(this, "госномер тягача", resources.getString(R.string.acc_truck_number)).setFixed(true), //

                new BranchPage(this, "повреждения грузовика?", resources.getString(R.string.acc_if_truck_damaged)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this,
                                        "повреждения грузовика",
                                        resources.getString(R.string.acc_photo_truck_damages))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new BranchPage(this, "повреждения полуприцепа?", resources.getString(R.string.acc_if_semitrailer_damaged)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this,
                                        "госномер полуприцепа",
                                        resources.getString(R.string.acc_photo_semitrailer_number)),
                                new ImagesChoicePage(this,
                                        "повреждения полуприцепа",
                                        resources.getString(R.string.acc_photo_semitrailer_damages))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new BranchPage(this, "повреждения груза?", resources.getString(R.string.acc_if_cargo_damaged)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this,
                                        "госномер полуприцепа",
                                        resources.getString(R.string.acc_photo_semitrailer_number)),
                                new ImagesChoicePage(this, "повреждения груза", resources.getString(R.string.acc_photo_cargo_damages))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new BranchPage(this, "повреждения других средств?", resources.getString(R.string.acc_if_other_vehicles_damaged)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this,
                                        "номер других средств",
                                        resources.getString(R.string.acc_photo_other_vehicles_numbers)),
                                new ImagesChoicePage(this,
                                        "повреждения других средств",
                                        resources.getString(R.string.acc_photo_other_vehicles_damages))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new BranchPage(this, "повреждения имуществу?", resources.getString(R.string.acc_if_other_property_damaged)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this,
                                        "фото повреждений",
                                        resources.getString(R.string.acc_photo_other_property_damages))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new BranchPage(this, "декларация?", resources.getString(R.string.acc_if_acc_declaration_exists)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this, "фото декларации", resources.getString(R.string.acc_photo_acc_declaration))) //
                        .addBranch(resources.getString(R.string.acc_no)),

                new ImagesChoicePage(this, "пояснения", resources.getString(R.string.acc_photo_sides_descriptions), 2), //

                new BranchPage(this, "другие объекты?", resources.getString(R.string.acc_if_other_objects_for_photo)) //
                        .addBranch(resources.getString(R.string.acc_yes),
                                new ImagesChoicePage(this, "доп фото", resources.getString(R.string.acc_photo_additional_objects))) //
                        .addBranch(resources.getString(R.string.acc_no)),
                new TextNotesPage(this, "замечания", resources.getString(R.string.acc_notes)));
    }
}
