The (somewhat unfortunately named) Interactive Screen and Interactive Elements form a rich ecosystem of completely custom UI elements, featuring interactivity, maintainability, and extensibility which the existing Forge UI elements lack so much. Heavily inspired by HTML and related technologies, working with the ChatMate Interactive UI should feel familiar and reasonably intuitive for a person familiar with these web development.

One of the main features of the Interactive Ecosystem (IE) is the dynamic layout engine. Each UI element can have assigned to it a number of layout properties which determine its size and position, and behaviour relative to parents, siblings, or children elements. These properties can be statically defined or dynamically changed - the layout engine attempts to size and position all elements on the Screen to satisfy all constraints and requirements, responding, if necessary, in real time to updates to these constraints and requirements.


# Rendering Lifecycle
The entire UI lifecycle is managed by the `InteractiveScreen`.

- Initialise
  - Subscribe to input events
  - Calculate initial layout [see below]
- Main rendering loop, called once per frame
  - While element layouts have been invalidated, re-calculate layouts and immediately perform side effects
  - Fire input events
  - Collect rendering callbacks
  - Execute rendering callbacks in order
  - Execute side effects in the order that were collected until there are none left
- Dispose
  - Unsubscribe from input events

Changing an element's layout properties automatically triggers the layout algorithm to be run. It does three passes through the element tree to finalise all element boxes - the first one top-down, the second one bottom-up, and the third one top-down.

In the first top-down pass, we recursively pass a parent's width down to the children via the `calculateSize(maxWidth)` method. The method parameter provides each element with the maximum width it can occupy. The deepest (primitive) elements use this information, together with their layout properties and state, to calculate its actual width and height. *It is a requirement that each element's `calculateSize` method gets called before rendering.*

In the bottom-up pass, each element passes its calculated box size back up to the parent. Elements with multiple children can arrange the children's box sizes via their own layout algorithm for calculating their own size.

In the final top-down pass, we recursively pass down the full box rectangle to each element, positioned in absolute space, via the `setBox(rectangle)` method. *It is a requirement that each element's `setBox` method gets called before rendering.*

All calculated sizes and positions are cached (retrievable via `getLastCalculatedSize()` and `getBox()`, respectively) and will be re-used until the layout gets invalidated. That is, `calculateSize` or `setBox` will not get called again until necessary.

# Layout
As in CSS, each element can be prescribed padding, a border, and a margin, in addition to its size.
types of boxes (full, border, where does mouse cursor change, etc)
- Content box: The box enclosing the element's contents.
- Padding box: The box enclosing the element's content box plus any padding.
- Border box: The box enclosing the element's padding box plus any borders.
- Full box: The box completely enclosing the element and all its extensions (padding, border, margin).

- Visible box: If set, specifies the box to which content should be clipped.
- Collision box: The intersection between the visible box and border box. Used to determine which mouse events should be relayed to the element.


## Side effects
- what is considered a side effect? changing the layout tree in some way during rendering. it is assumed that the tree is static during the rendering sequence. IE events are executed before (?) the rendering sequence, so it is safe to perform side effects directly. in async fns (e.g. callbacks), must defer side effects. will be executed in order after rednering finishes. can perform side effects during layout calculation phase (e.g. calculateSize(), setBox()), but care should be taken as it will trigger another pass of the layout algo and can easiliy result in an infinite loop.

## Units
There are two types of units used to specify sizes: GUI and screen units. GUI units scale with Minecraft, while screen units are anchored to the screen pixel coordinates. The `Dim` class (short for dimension) solves the problem of mixing up these units and having to do repetitve conversions. All methods working with a length or size of some sort will accept `Dim`s instead of primitive numbers in their public API. When instantiating a `Dim` object, the unit type is specified, and unit conversions are conducted automatically when performing math on these objects.

# Events
The `InteractiveScreen` listens for mouse, keyboard, and screen resize events.

# Anatomy of a Typical Element
The majority of elements are compound elements inheriting from the `ContainerElement` to group together more primitive elements inheriting from the `SingleElement`, such as buttons or labels. Since every element implements the `IElement` interface, it is straightforward to create reusable elements akin to React (or similar frameworks') components. In fact, just like React (class) components, Interactive Elements specify the content to render in the `render()` method. A default implementation of the `IElement` interface is provided by the `ElementBase` class. This includes automatic handling of sizing the various boxes, provides hooks for user interaction events, cursor/pointer management, state management, and some related helper methods.

# Rendering
Each frame, the rendering engine calls the `render()` method of each visible element. Note that all low-level rendering is [deferred on the initial pass](#rendering-lifecycle), and executed in the order collected grouped by effective `zIndex`, starting at the lowest `zIndex` and working up.

The rendered content of each element is culled to its effective visible box - the intersection between the element's own visible box (if set) and the parent's effective visible box. Truncation is achieved using the OpenGL scissors API.

