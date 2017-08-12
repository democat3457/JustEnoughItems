package mezz.jei.test;

import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestIngredient;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.test.lib.TestPlugin;
import net.minecraft.util.NonNullList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IngredientFilterTest {
	private static final int EXTRA_INGREDIENT_COUNT = 5;
	@Nullable
	private IModIdHelper modIdHelper;
	@Nullable
	private IngredientRegistry ingredientRegistry;
	@Nullable
	private IngredientFilter ingredientFilter;

	@Before
	public void setup() {
		TestPlugin testPlugin = new TestPlugin();

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
		testPlugin.registerItemSubtypes(subtypeRegistry);

		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		testPlugin.registerIngredients(modIngredientRegistry);

		this.modIdHelper = new TestModIdHelper();
		this.ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper);

		NonNullList<IIngredientListElement> baseList = IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		JeiHelpers jeiHelpers = new JeiHelpers(ingredientRegistry, stackHelper);

		this.ingredientFilter = new IngredientFilter(jeiHelpers);
		this.ingredientFilter.addIngredients(baseList);
	}

	@Test
	public void testSetup() {
		Assert.assertNotNull(ingredientFilter);

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	@Test
	public void testAddingIngredients() {
		Assert.assertNotNull(ingredientFilter);
		addIngredients(ingredientFilter);
		removeIngredients(ingredientFilter);
	}

	@Test
	public void testAddingAndRemovingIngredients() {
		Assert.assertNotNull(ingredientFilter);
		addIngredients(ingredientFilter);
		removeIngredients(ingredientFilter);
	}

	@Test
	public void testRebuilding() {
		Assert.assertNotNull(ingredientFilter);

		ingredientFilter.modesChanged();

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		addIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());

		removeIngredients(ingredientFilter);

		ingredientFilter.modesChanged();

		ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());
	}

	private void addIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(modIdHelper);

		List<TestIngredient> ingredientsToAdd = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToAdd.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToAdd.size());

		List<IIngredientListElement> listToAdd = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.class, ingredientsToAdd, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToAdd.size());

		ingredientRegistry.addIngredientsAtRuntime(TestIngredient.class, ingredientsToAdd, ingredientFilter);

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.class);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, testIngredients.size());

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT, ingredientList.size());
	}

	private void removeIngredients(IngredientFilter ingredientFilter) {
		Assert.assertNotNull(ingredientRegistry);
		Assert.assertNotNull(modIdHelper);

		List<TestIngredient> ingredientsToRemove = new ArrayList<>();
		for (int i = TestPlugin.BASE_INGREDIENT_COUNT; i < TestPlugin.BASE_INGREDIENT_COUNT + EXTRA_INGREDIENT_COUNT; i++) {
			ingredientsToRemove.add(new TestIngredient(i));
		}
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, ingredientsToRemove.size());

		List<IIngredientListElement> listToRemove = IngredientListElementFactory.createList(ingredientRegistry, TestIngredient.class, ingredientsToRemove, modIdHelper);
		Assert.assertEquals(EXTRA_INGREDIENT_COUNT, listToRemove.size());

		ingredientRegistry.removeIngredientsAtRuntime(TestIngredient.class, ingredientsToRemove, ingredientFilter);

		List<IIngredientListElement> ingredientList = ingredientFilter.getIngredientList();
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, ingredientList.size());

		Collection<TestIngredient> testIngredients = ingredientRegistry.getAllIngredients(TestIngredient.class);
		Assert.assertEquals(TestPlugin.BASE_INGREDIENT_COUNT, testIngredients.size());
	}
}
