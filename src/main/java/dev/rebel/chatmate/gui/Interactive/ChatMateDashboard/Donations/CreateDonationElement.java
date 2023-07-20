package dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.Donations;

import dev.rebel.chatmate.api.models.donation.CreateDonationRequest;
import dev.rebel.chatmate.api.models.donation.CreateDonationResponse.CreateDonationsResponseData;
import dev.rebel.chatmate.api.models.donation.GetCurrenciesResponse.GetCurrenciesResponseData;
import dev.rebel.chatmate.api.proxy.DonationEndpointProxy;
import dev.rebel.chatmate.api.proxy.EndpointProxy;
import dev.rebel.chatmate.api.publicObjects.donation.PublicCurrency;
import dev.rebel.chatmate.gui.Interactive.ButtonElement.TextButtonElement;
import dev.rebel.chatmate.gui.Interactive.*;
import dev.rebel.chatmate.gui.Interactive.ChatMateDashboard.SharedElements;
import dev.rebel.chatmate.gui.Interactive.Layout.*;
import dev.rebel.chatmate.gui.style.Colour;
import dev.rebel.chatmate.util.Collections;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.rebel.chatmate.util.Objects.castedVoid;
import static dev.rebel.chatmate.util.Objects.firstOrNull;

public class CreateDonationElement extends ContainerElement {
  private final Runnable onDone;
  private final TextInputElement nameElement;
  private final DonationAmountElement donationAmountElement;
  private final TextInputElement messageElement;
  private final LabelElement errorLabel;
  private final LoadingSpinnerElement loadingSpinnerElement;
  private final TextButtonElement cancelButton;
  private final TextButtonElement submitButton;

  private DonationAmountModel donationAmountModel;

  public CreateDonationElement(InteractiveScreen.InteractiveContext context, IElement parent, Runnable onDone, DonationEndpointProxy donationEndpointProxy) {
    super(context, parent, LayoutMode.BLOCK);
    super.setSizingMode(SizingMode.FILL);

    this.onDone = onDone;
    this.donationAmountModel = new DonationAmountModel(null, null);

    IElement titleLabel = new LabelElement(context, this)
        .setText("Create a Donation")
        .setMargin(RectExtension.fromBottom(gui(16)))
        .setHorizontalAlignment(HorizontalAlignment.CENTRE);
    this.nameElement = new TextInputElement(context, this)
        .onTextChange(this::onNameChanged)
        .cast();
    this.donationAmountElement = new DonationAmountElement(context, this, donationEndpointProxy, this::onDonationAmountChanged)
        .cast();
    this.messageElement = new TextInputElement(context, this)
        .setPlaceholder("Optional donation message")
        .onTextChange(this::onMessageChanged)
        .cast();

    this.errorLabel = SharedElements.ERROR_LABEL.create(context, this)
        .setMargin(RectExtension.fromBottom(gui(8)))
        .cast();
    this.loadingSpinnerElement = new LoadingSpinnerElement(context, this)
        .setVisible(false)
        .setMargin(RectExtension.fromLeft(gui(8)))
        .setVerticalAlignment(VerticalAlignment.MIDDLE)
        .cast();
    this.cancelButton = new TextButtonElement(context, this)
        .setText("Cancel")
        .setOnClick(this::onCancel)
        .cast();
    this.submitButton = new TextButtonElement(context, this)
        .setText("Create")
        .setEnabled(this, false)
        .setOnClick(this::onSubmit)
        .cast();

    super.addElement(titleLabel);
    super.addElement(new SideBySideElement(context, this)
      .addElement(1, new LabelElement(context, this)
          .setText("Name:")
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
      ).addElement(3, this.nameElement)
      .setMargin(RectExtension.fromBottom(gui(8)))
    );
    super.addElement(new SideBySideElement(context, this)
        .addElement(1, new LabelElement(context, this)
            .setText("Amount:")
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(3, this.donationAmountElement)
        .setMargin(RectExtension.fromBottom(gui(8)))
    );
    super.addElement(new SideBySideElement(context, this)
        .addElement(1, new LabelElement(context, this)
            .setText("Message:")
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
        ).addElement(3, this.messageElement)
        .setMargin(RectExtension.fromBottom(gui(16)))
    );
    super.addElement(this.errorLabel);
    super.addElement(new InlineElement(context, this)
        .addElement(this.cancelButton)
        .addElement(this.submitButton)
        .addElement(this.loadingSpinnerElement)
    );
  }

  private void onNameChanged(String name) {
    this.onModelUpdated();
  }

  private void onDonationAmountChanged(DonationAmountModel model) {
    this.donationAmountModel = model;
    this.onModelUpdated();
  }

  private void onMessageChanged(String message) {
    this.onModelUpdated();
  }

  private void onModelUpdated() {
    boolean isValid = this.donationAmountModel.isValid && this.nameElement.getText().trim().length() > 0;
    boolean isLoading = this.loadingSpinnerElement.getVisible();
    this.submitButton.setEnabled(this, isValid && !isLoading);
  }

  private void onCancel() {
    this.onDone.run();
  }

  private void onSubmit() {
    CreateDonationRequest request = new CreateDonationRequest(
        this.donationAmountModel.amount,
        this.donationAmountModel.currencyCode,
        this.nameElement.getText(),
        this.messageElement.getText()
    );
    super.context.donationApiStore.createDonation(request, this::onCreateResponse, this::onCreateError);

    this.loadingSpinnerElement.setVisible(true);
    this.errorLabel.setVisible(false);
    this.submitButton.setEnabled(this, false);
    this.cancelButton.setEnabled(this, false);
    this.nameElement.setEnabled(this, false);
    this.messageElement.setEnabled(this, false);
    this.donationAmountElement.setEnabled(false);
  }

  private void onCreateResponse(CreateDonationsResponseData data) {
    super.context.renderer.runSideEffect(this.onDone);
  }

  private void onCreateError(Throwable error) {
    super.context.renderer.runSideEffect(() -> {
      this.loadingSpinnerElement.setVisible(false);
      this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error)).setVisible(true);
      this.submitButton.setEnabled(this, true);
      this.cancelButton.setEnabled(this, true);
      this.nameElement.setEnabled(this, true);
      this.messageElement.setEnabled(this, true);
      this.donationAmountElement.setEnabled(true);
    });
  }

  private static class DonationAmountElement extends ContainerElement {
    private final Consumer<DonationAmountModel> onChange;

    private final TextInputElement amountInputElement;
    private final DropdownSelectionElement<PublicCurrency> currencyDropdown;
    private final LabelElement errorLabel;

    public DonationAmountElement(InteractiveScreen.InteractiveContext context, IElement parent, DonationEndpointProxy donationEndpointProxy, Consumer<DonationAmountModel> onChange) {
      super(context, parent, LayoutMode.INLINE);
      this.onChange = onChange;

      this.amountInputElement = new TextInputElement(context, this)
          .setTextUnsafe("5.00")
          .setValidator(this::onValidateAmount)
          .onTextChange(this::onAmountChanged)
          .setMaxContentWidth(gui(50))
          .cast();

      // hack: the dropdown selection box is narrow, but we want dropdown menu items to be wider to display more info.
      // to get around this, add an invisible, wide element to which the dropdown menu should be anchored.
      IElement dropdownAnchorElement = new EmptyElement(context, this)
          .setWidth(gui(50))
          .setLayoutGroup(LayoutGroup.CHILDREN)
          .setVerticalAlignment(VerticalAlignment.BOTTOM)
          .setMargin(RectExtension.fromLeft(gui(8)));
      this.currencyDropdown = new DropdownSelectionElement<>(context, this)
          .setEnabled(this, false)
          .setMaxContentWidth(gui(40))
          .setPadding(RectExtension.fromLeft(gui(8)))
          .setVerticalAlignment(VerticalAlignment.MIDDLE)
          .cast();
      this.currencyDropdown.dropdownMenu.setAnchorElement(dropdownAnchorElement).setSizingMode(SizingMode.MINIMISE);

      this.errorLabel = SharedElements.ERROR_LABEL.create(context, this)
          .setSizingMode(SizingMode.MINIMISE)
          .setMargin(RectExtension.fromTop(gui(4)).left(gui(4)))
          .cast();
      super.addElement(this.amountInputElement)
          .addElement(dropdownAnchorElement)
          .addElement(this.currencyDropdown)
          .addElement(this.errorLabel);

      donationEndpointProxy.getCurrenciesAsync(this::onGetCurrenciesResponse, this::onGetCurrenciesError, true);
    }

    public void setEnabled(boolean enabled) {
      this.amountInputElement.setEnabled(this, enabled);
      this.currencyDropdown.setEnabled(this, enabled);
    }

    private boolean onValidateAmount(String text) {
      text = text.trim();
      if (text.equals("")) {
        return true;
      }

      try {
        Float.parseFloat(text);
        return text.length() < 10;
      } catch (Exception ignored) {
        return false;
      }
    }

    private void onGetCurrenciesResponse(GetCurrenciesResponseData data) {
      super.context.renderer.runSideEffect(() -> {
        this.currencyDropdown.setEnabled(this, true);
        //this.currencyDropdown.dropdownMenu.setMinWidth(gui(25));

        for (PublicCurrency currency : data.currencies) {
          this.currencyDropdown.addOption(
              new BlockElement(context, this)
                  .addElement(new LabelElement(context, this)
                      .setText(currency.code)
                      .setFontScale(0.75f)
                      .setSizingMode(SizingMode.FILL)
                  ).addElement(new LabelElement(context, this)
                      .setText(currency.description)
                      .setColour(Colour.GREY50)
                      .setFontScale(0.5f)
                      .setPadding(RectExtension.fromTop(gui(2)))
                  ).setMargin(new RectExtension(gui(2))),
              currency,
              this::onSelectCurrency,
              (el, isSelected) -> {
                List<IElement> children = firstOrNull(el.getChildren(), Collections.list());
                assert children != null;
                castedVoid(LabelElement.class, children.get(0), label -> label.setColour(isSelected ? Colour.LIGHT_YELLOW : Colour.WHITE));
                castedVoid(LabelElement.class, children.get(1), label -> label.setColour(isSelected ? Colour.GREY75 : Colour.GREY50));
              },
              c -> c.code
          );
        }

        @Nullable PublicCurrency defaultSelection = Collections.first(Collections.list(data.currencies), c -> Objects.equals(c.code, "USD"));
        this.currencyDropdown.setSelection(firstOrNull(defaultSelection, data.currencies[0]));

        this.onChange.accept(this.getModel());
      });
    }

    private void onGetCurrenciesError(Throwable error) {
      // this is a one-off component, we don't show a retry button. to refresh, the user must go back
      super.context.renderer.runSideEffect(() -> {
        this.errorLabel.setText(EndpointProxy.getApiErrorMessage(error))
            .setVisible(true);
      });
    }

    private void onSelectCurrency(PublicCurrency currency) {
      this.onChange.accept(this.getModel());
    }

    private void onAmountChanged(String newAmount) {
      this.onChange.accept(this.getModel());
    }

    private DonationAmountModel getModel() {
      @Nullable Float amount = null;
      try {
        amount = Float.parseFloat(this.amountInputElement.getText());
      } catch (Exception ignored) { }

      @Nullable PublicCurrency selectedCurrency = this.currencyDropdown.getSelection();
      @Nullable String currency = selectedCurrency == null ? null : selectedCurrency.code;

      return new DonationAmountModel(amount, currency);
    }
  }

  private static class DonationAmountModel {
    public final @Nullable Float amount;
    public final @Nullable String currencyCode;
    public final boolean isValid;

    private DonationAmountModel(@Nullable Float amount, @Nullable String currencyCode) {
      this.amount = amount;
      this.currencyCode = currencyCode;
      this.isValid = amount != null && amount > 0 && amount < 100000 && currencyCode != null;
    }
  }
}
